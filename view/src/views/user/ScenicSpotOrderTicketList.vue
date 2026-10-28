<template>

    <div class="user-orders">
        <!-- 查询条件 -->
        <div style="margin-bottom: 20px;display: flex;justify-content: space-between;align-content: center;">
            <Tab :buttons="[
                { label: '全部', value: 'null' },
                { label: '待付款', value: '1' },
                { label: '已核销', value: '2' },
                { label: '待使用', value: '3' },
                { label: '已取消', value: '4' },
                { label: '退款中', value: '5' },
                { label: '已退款', value: '6' },
                { label: '拒绝退款', value: '7' },
            ]" :initialActive="'null'" @change="handleChange" />
            <el-date-picker @change="handleDateChange" style="max-width: 300px;" v-model="dateRanges" type="daterange"
                range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期">
            </el-date-picker>
        </div>
        <div>
            <el-empty v-if="!scenicSpotTicketOrdersList.length" description="暂无订单信息"></el-empty>
        </div>
        <!-- 实际的订单区域 -->
        <div :key="`ticketOrder-${ticketOrder.id}`" v-for="ticketOrder in scenicSpotTicketOrdersList"
            class="ticket-order-card">

            <!-- 订单内容 -->
            <div class="order-content">
                <!-- 门票基本信息 -->
                <div class="ticket-info">
                    <div class="price-section">
                        <span class="price">¥{{ ticketOrder.price?.toFixed(2) || '0.00' }}</span>
                        <span class="buy-number" v-if="ticketOrder.buyNumber">×门票*{{ ticketOrder.buyNumber }}</span>
                        <span>
                            <el-countdown v-if="Number(ticketOrder.status) === 1"
                                :value="getDeadlineTime(ticketOrder.createTime as any)" format="mm分ss秒" />
                        </span>
                    </div>

                    <!-- 使用日期 -->
                    <div class="date-info" v-if="ticketOrder.startDate">
                        <el-tag type="warning">{{ getStatusText(ticketOrder.status as any) }}</el-tag>
                        <span class="date-icon">门票有效期
                            {{ ticketOrder.startDate }} - {{ ticketOrder.endDate }}
                        </span>
                    </div>

                    <!-- 联系人信息 -->
                    <div class="contact-info" v-if="ticketOrder.concatUsername || ticketOrder.concatPhone">
                        <div v-if="ticketOrder.concatUsername" class="contact-item">
                            联系人：{{ ticketOrder.concatUsername }} - 联系电话：{{ ticketOrder.concatPhone }}
                        </div>
                        <div v-if="ticketOrder.concatPhone" class="contact-item">
                            身份证号：{{ ticketOrder.idcard }}
                        </div>
                    </div>
                </div>

                <!-- 按钮操作区域 -->
                <div style="margin-top: 10px;">
                    <!-- 订单处在待支付状态时，可取消 -->
                    <el-button v-if="ticketOrder.status === 1" type="info" round size="mini"
                        @click="cancelOrders(ticketOrder)">取消订单</el-button>
                    <!-- 订单处在待支付状态时才需要进行显示-支付 -->
                    <el-button v-if="ticketOrder.status === 1" type="success" round size="mini"
                        @click="payOrders(ticketOrder)">立即支付</el-button>
                    <!-- 订单处在已支付且不存在退款中的记录 -->
                    <el-button v-if="refundShowCondition(ticketOrder)" type="warning" round size="mini"
                        @click="refundReply(ticketOrder)">申请退款</el-button>
                    <!-- 订单处在待支付状态或已取消状态时才需要进行显示-删除订单 -->
                    <el-button v-if="ticketOrder.status === 1 || ticketOrder.status === 4" type="danger" round
                        size="mini" @click="removeOrders(ticketOrder)">删除订单</el-button>
                </div>

                <!-- 订单时间线 -->
                <div class="order-timeline" style="margin-bottom: 10px;">
                    <div class="time-item" v-if="ticketOrder.createTime">
                        <span class="time-label">创建时间：{{ ticketOrder.createTime }}</span>
                    </div>
                    <div class="time-item" v-if="ticketOrder.payTime">
                        <span class="time-label">支付时间：{{ ticketOrder.payTime }}</span>
                    </div>
                    <div class="time-item" v-if="ticketOrder.useTime">
                        <span class="time-label">核销时间：{{ ticketOrder.useTime }} </span>
                    </div>
                    <div class="time-item" v-if="ticketOrder.cancelTime">
                        <span class="time-label">取消时间：{{ ticketOrder.cancelTime }} </span>
                    </div>
                </div>
            </div>

            <div style="padding: 6px 16px;font-size: 18px;font-weight: bold;" v-if="ticketOrder.orderRefundInfoList.length">
                退款时间轴
            </div>
            <!-- 订单退款申请记录区域 -->
            <div class="refund-container" v-if="ticketOrder.orderRefundInfoList.length">
                <el-steps direction="vertical" :active="ticketOrder.orderRefundInfoList.length">
                    <el-step v-for="(refundReply, index) in ticketOrder.orderRefundInfoList"
                        :key="`refund-reply-${index}`"
                        :title="refundReply.status === 1 ? '退款中' : refundReply.status === 2 ? '已退款' : '拒绝退款'"
                        :description="getStepDescription(refundReply)" :icon="getStatusIcon(refundReply.status as any)"
                        :status="getStepStatus(refundReply.status as any)">
                        <template #description>
                            <div class="step-description">
                                <div class="create-time">申请时间：{{ refundReply.createTime }}</div>

                                <!-- 拒绝退款时的额外信息 -->
                                <div v-if="refundReply.status === 3" class="reject-details">
                                    <div class="reject-reason">
                                        <span>拒绝缘由：{{ refundReply.rejectReason || '未提供拒绝原因' }}</span>
                                    </div>
                                    <div class="reject-time">
                                        <span>审核时间：{{ refundReply.rejectRefundTime || '未记录' }}</span>
                                    </div>
                                </div>

                                <!-- 已退款时的信息 -->
                                <div v-if="refundReply.status === 2" class="success-details">
                                    <div class="refund-time">
                                        <el-icon>
                                            <Clock />
                                        </el-icon>
                                        <span>到账时间：{{ refundReply.refundTime || refundReply.createTime }}</span>
                                    </div>
                                </div>
                            </div>
                        </template>
                    </el-step>
                </el-steps>
            </div>

            <!-- 详情展开 -->
            <div class="order-detail" v-if="ticketOrder.detail">
                <details>
                    <summary class="detail-summary">
                        <span>门票详情</span>
                        <i class="arrow">▼</i>
                    </summary>
                    <div class="detail-content">
                        <pre>{{ ticketOrder.detail }}</pre>
                    </div>
                </details>
            </div>
        </div>

        <!-- 分页区域 -->
        <div v-if="scenicSpotTicketOrdersList.length">
            <div class="pagination">
                <el-pagination v-model:current-page="scenicSpotTicketOrderQueryDTO.current"
                    v-model:page-size="scenicSpotTicketOrderQueryDTO.size" :page-sizes="[5, 10]" :total="totalPageCount"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </div>
    </div>

</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { handleDateRange } from "@/utils/date"
import { ElMessageBox, ElMessage } from "element-plus"
import {
    Connection,
    CircleCheckFilled,
    CircleCloseFilled,
    Warning,
    Clock,
    Money
} from '@element-plus/icons-vue'
import Tab from "@/components/Tab.vue"
import type {
    ScenicSpotTicketOrder,
    ScenicSpotTicketOrderVO,
    ScenicSpotTicketOrderQueryDTO,
    OrderRefundInfo
} from "@/types/scenic-spot-ticket-order"
import {
    updateOrderStatusApi,
    queryUserScenicSpotTicketOrdersApi
} from "@/api/scenic-spot-ticket-orders-api"

// 响应式数据
const scenicSpotTicketOrderQueryDTO = reactive<ScenicSpotTicketOrderQueryDTO>({
    size: 5,
    current: 1
})
const scenicSpotTicketOrdersList = ref<ScenicSpotTicketOrderVO[]>([])
const totalPageCount = ref<number | undefined>(0) // 总记录数
const dateRanges = ref<Date[]>([])


//1-待付款；2-已核销；3-待使用；4-已取消
const getStatusText = (status: number) => {
    if (status === 1) {
        return "待付款"
    } else if (status === 2) {
        return "已核销"
    } else if (status === 3) {
        return "待使用"
    } else if (status === 4) {
        return "已取消"
    } else {
        return "未知状态"
    }
}

// 计算截止时间（15分钟后）
const getDeadlineTime = (createTime: string): number => {
    // 1. 将 "2026-01-06 16:50:29" 转为时间戳
    const createTimestamp = new Date(createTime).getTime()

    // 2. 加上15分钟（900000毫秒）
    return createTimestamp + 15 * 60 * 1000
}

const handleChange = async (obj: { value: string }) => {
    scenicSpotTicketOrderQueryDTO.status = obj.value === 'null' ? null : Number(obj.value)
    scenicSpotTicketOrderQueryDTO.current = 1
    await fetchScenicSpotTicketOrder()
}

const handleSizeChange = async (size: number) => {
    scenicSpotTicketOrderQueryDTO.size = size
    scenicSpotTicketOrderQueryDTO.current = 1
    await fetchScenicSpotTicketOrder()
}

const handleCurrentChange = async (current: number) => {
    scenicSpotTicketOrderQueryDTO.current = current
    await fetchScenicSpotTicketOrder()
}

// 订单支付
const payOrders = async (ticketOrder: ScenicSpotTicketOrder) => {
    try {
        const response = await ElMessageBox.confirm(
            `确定要支付"【${ticketOrder.price}元】"完成该订单的支付？`,
            '确认',
            {
                confirmButtonText: '我确定支付',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
            }
        )
        if (response === 'confirm') {
            try {
                // 支付 - 变更订单状态 - 待使用
                await updateOrderStatusApi(ticketOrder.id as any, "pay")
                ElMessage.success('订单支付成功')

            } catch (error: any) {
                ElMessage.warning(`下单异常：${error.message}`)
                console.error("下单异常：", error)

            } finally {
                await fetchScenicSpotTicketOrder()
            }

        }
    } catch (error) {
        // 用户取消
    }
}

// 订单删除
const removeOrders = async (ticketOrder: ScenicSpotTicketOrder) => {
    try {
        const response = await ElMessageBox.confirm(
            `确定要删除该订单？`,
            '确认',
            {
                confirmButtonText: '我确定删除',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
            }
        )
        if (response === 'confirm') {
            try {
                // 支付 - 变更订单状态 - 待使用
                await updateOrderStatusApi(ticketOrder.id as any, "remove")
                ElMessage.success('订单删除成功')

            } catch (error: any) {
                ElMessage.warning(`删除异常：${error.message}`)
                console.error("删除异常：", error)

            } finally {
                await fetchScenicSpotTicketOrder()
            }

        }
    } catch (error) {
        // 用户取消
    }
}

// 申请退款
const refundReply = async (ticketOrder: ScenicSpotTicketOrder) => {
    try {
        const response = await ElMessageBox.confirm(
            `确定要申请退款？`,
            '确认',
            {
                confirmButtonText: '我确定提交退款申请',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
            }
        )
        if (response === 'confirm') {
            try {
                // 退款 - 变更订单状态 - 退款中
                await updateOrderStatusApi(ticketOrder.id as any, "refund")
                ElMessage.success('订单申请退款成功')

            } catch (error: any) {
                ElMessage.warning(`申请退款异常：${error.message}`)
                console.error("申请退款异常：", error)

            } finally {
                await fetchScenicSpotTicketOrder()
            }

        }
    } catch (error) {
        // 用户取消删除
        console.log('取消删除')
    }
}

// 订单支付
const cancelOrders = async (ticketOrder: ScenicSpotTicketOrder) => {
    try {
        const response = await ElMessageBox.confirm(
            `确定取消该订单吗？`,
            '确认',
            {
                confirmButtonText: '我确定取消',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
            }
        )
        if (response === 'confirm') {
            try {
                // 支付 - 变更订单状态 - 待使用
                await updateOrderStatusApi(ticketOrder.id as any, "cancel")
                ElMessage.success('订单取消成功')

            } catch (error: any) {
                ElMessage.warning(`订单取消异常：${error.message}`)
                console.error("订单取消异常：", error)

            } finally {
                await fetchScenicSpotTicketOrder()
            }

        }
    } catch (error) {
        // 用户取消
    }
}

const refundShowCondition = (ticketOrder: ScenicSpotTicketOrder) => {
    // 当前的状态处在待使用且下面没有退款中的申请，显示退款按钮
    // 1 - 退款中；3-拒绝推荐；2-已退款
    const refunded = ticketOrder.orderRefundInfoList.some((order: OrderRefundInfo) => order.status === 2)
    if (refunded) {
        return
    }
    const statusFlag = ticketOrder.status === 3
    const result = ticketOrder.orderRefundInfoList.some((order: OrderRefundInfo) => order.status === 1)
    return statusFlag && !result
}

const handleDateChange = async () => {
    const dates = handleDateRange(dateRanges.value)
    if (dates) {
        scenicSpotTicketOrderQueryDTO.startTime = dates.start
        scenicSpotTicketOrderQueryDTO.endTime = dates.end
    } else {
        scenicSpotTicketOrderQueryDTO.startTime = null
        scenicSpotTicketOrderQueryDTO.endTime = null
    }
    await fetchScenicSpotTicketOrder()
}

// 查询用户名下产生的景点门票订单数据
const fetchScenicSpotTicketOrder = async () => {
    try {
        const { data, total } = await queryUserScenicSpotTicketOrdersApi(scenicSpotTicketOrderQueryDTO)
        scenicSpotTicketOrdersList.value = data
        totalPageCount.value = total
    } catch (error: any) {
        console.error("查询用户对于景点的收藏状态异常:", error)
    }
}

// 获取步骤图标
const getStatusIcon = (status: number) => {
    switch (status) {
        case 1: return Connection
        case 2: return CircleCheckFilled
        case 3: return CircleCloseFilled
        default: return Connection
    }
}

// 获取步骤状态
const getStepStatus = (status: number) => {
    switch (status) {
        case 1: return 'process'  // 进行中
        case 2: return 'finish'   // 已完成
        case 3: return 'error'    // 错误/拒绝
        default: return 'wait'    // 等待中
    }
}

// 获取步骤描述（基础描述）
const getStepDescription = (refundReply: any) => {
    return `申请时间：${refundReply.createTime}`
}

onMounted(async () => {
    // 查询用户名下产生景点门票订单数据
    await fetchScenicSpotTicketOrder()
})

</script>
<style lang="scss" scoped>
.step-description {
    padding: 8px 0;

    .create-time {
        color: #666;
        font-size: 14px;
        margin-bottom: 8px;
        display: flex;
        align-items: center;
        gap: 6px;

        // &::before {
        //     content: "🕐";
        //     font-size: 12px;
        // }
    }

    .reject-details {
        background: #fff5f5;
        border: 1px solid #fed7d7;
        border-radius: 6px;
        padding: 10px;
        margin-top: 8px;

        .reject-reason {
            color: #e53e3e;
            font-size: 14px;
            display: flex;
            align-items: flex-start;
            gap: 6px;
            margin-bottom: 6px;

            .el-icon {
                flex-shrink: 0;
                margin-top: 2px;
            }
        }

        .reject-time {
            color: #718096;
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 6px;

            .el-icon {
                flex-shrink: 0;
            }
        }
    }

    .success-details {
        background: #f0fff4;
        border: 1px solid #c6f6d5;
        border-radius: 6px;
        padding: 10px;
        margin-top: 8px;

        .refund-amount {
            color: #38a169;
            font-size: 14px;
            font-weight: 500;
            display: flex;
            align-items: center;
            gap: 6px;
            margin-bottom: 6px;

            .el-icon {
                flex-shrink: 0;
            }
        }

        .refund-time {
            color: #718096;
            font-size: 13px;
            display: flex;
            align-items: center;
            gap: 6px;

            .el-icon {
                flex-shrink: 0;
            }
        }
    }
}

// 自定义步骤状态样式
:deep(.el-step__head) {
    &.is-process {
        .el-step__icon {
            background-color: none;
            border-color: #1890ff;
            color: #1890ff;
        }
    }

    &.is-finish {
        .el-step__icon {
            background-color: none;
            border-color: #52c41a;
            color: #52c41a;
        }
    }

    &.is-error {
        .el-step__icon {
            background-color: none;
            border-color: #ff4d4f;
            color: #ff4d4f;
        }
    }
}

:deep(.el-step__line) {
    background-color: #f0f0f0;
}


:deep(.el-step__description) {
    font-size: 14px;
}


.user-orders {
    padding-inline: 160px;
    box-sizing: border-box;
}

.refund-container {
    display: flex;
    justify-content: left;
    align-items: center;
    // gap: 20px;
    margin: 0 10px 20px 10px;
    overflow-x: auto;
    padding: 10px 8px;
}

.item-refund {
    display: flex;
    align-items: center;
    // min-height: 100px;
    min-width: 200px;
}

.refund-block {
    padding: 10px;
    min-height: 150px;
    min-width: 200px;
    box-sizing: border-box;
    // background-color: rgb(248, 248, 248);
    border-radius: 4px;
    // border: 1px solid #e8e8e8;
}

.line {
    width: 30px;
    height: 1px;
    background-color: #a8a6a6;
    flex-shrink: 0;
}

.status {
    font-weight: bold;
    margin-bottom: 8px;
    font-size: 14px;
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;
}

.reject-info {
    margin: 8px 0;
    // padding: 8px;
    // background-color: #fff;
    border-radius: 4px;
    // border-left: 3px solid #f56c6c;
}

.reject-reason {
    color: #f56c6c;
    font-size: 13px;
    margin-bottom: 4px;
}

.reject-time {
    color: #999;
    font-size: 12px;
}

.time {
    font-size: 12px;
    color: #666;
    margin-top: 8px;
}

/* 状态颜色 */
.refund-block .status[data-status="1"] {
    color: #e6a23c;
    /* 退款中 - 橙色 */
}

.refund-block .status[data-status="2"] {
    color: #67c23a;
    /* 已退款 - 绿色 */
}

.refund-block .status[data-status="3"] {
    color: #f56c6c;
    /* 拒绝退款 - 红色 */
}


// .pagination {
//     display: flex;
//     justify-content: flex-end;
// }

/* 简约样式 */
.ticket-order-card {
    background: #fff;
    border-radius: 12px;
    // box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 16px;
    overflow: hidden;
    transition: box-shadow 0.3s ease;
    border: 1px dashed rgb(230, 230, 230);
}

.ticket-order-card:hover {
    // box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
    border: 1px dashed rgb(197, 196, 196);
}

/* 订单头部 */
.order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid rgba(255, 255, 255, 0.3);
}

.username {
    font-size: 16px;
    font-weight: 500;
}

/* 订单状态 */
.order-status {
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
}

.status-2 {
    background: rgba(34, 197, 94, 0.2);
    color: #22c55e;
}

/* 订单内容 */
.order-content {
    padding: 20px 16px 0 16px;
}

.price-section {
    display: flex;
    align-items: baseline;
    gap: 8px;
}

.price {
    font-size: 38px;
    font-weight: bold;
    color: #f97316;
}

.buy-number {
    font-size: 14px;
    color: #666;
}

/* 日期信息 */
.date-info {
    display: flex;
    align-items: center;
    gap: 8px;
    // padding: 8px 12px;
    // background: #f8fafc;
    border-radius: 8px;
    margin-bottom: 12px;
}

.date-icon {
    font-size: 14px;
    color: #a1a2a3;
}

.date-text {
    font-size: 14px;
    color: #475569;
}

/* 联系人信息 */
.contact-info {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 12px;
    font-size: 16px;
    font-weight: 600;
    background-color: rgb(250, 250, 250);
    border: 1px solid rgb(232, 232, 232);
    padding: 20px;
    box-sizing: border-box;
    border-radius: 20px;
}

.contact-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    color: #333;
    font-weight: 600;
}

.contact-icon {
    font-size: 12px;
    opacity: 0.7;
}

/* 订单时间线 */


.time-item {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 6px;
}

.time-label {
    color: #959799;
}

.time-value {
    color: #475569;
    font-family: 'Courier New', monospace;
}

/* 详情展开 */
.order-detail {
    border-top: 1px solid #e2e8f0;
    padding: 0 16px;
}

.detail-summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    cursor: pointer;
    list-style: none;
    font-size: 14px;
    color: #64748b;
    transition: color 0.3s ease;
}

.detail-summary:hover {
    color: #475569;
}

.detail-summary::-webkit-details-marker {
    display: none;
}

.arrow {
    transition: transform 0.3s ease;
    font-size: 10px;
}

details[open] .arrow {
    transform: rotate(180deg);
}

.detail-content {
    padding: 12px 0 16px;
    border-top: 1px solid #f1f5f9;
}

.detail-content pre {
    margin: 0;
    white-space: pre-wrap;
    font-size: 13px;
    line-height: 1.5;
    color: #475569;
    font-family: inherit;
    background: #f8fafc;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
}

/* 空状态处理 */
.ticket-order-card:empty {
    display: none;
}

/* 响应式设计 */
@media (max-width: 640px) {
    .order-header {
        padding: 12px;
    }

    .order-content {
        padding: 16px 12px;
    }

    .price {
        font-size: 24px;
    }

    .avatar {
        width: 36px;
        height: 36px;
    }
}
</style>