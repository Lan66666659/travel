<template>
    <div class="scenic-route-detail-container">
        <!-- 景点门票项列表 -->
        <div>
            <div v-if="!scenicSpotTicketList.length">
                <el-empty description="无景点门票数据"></el-empty>
            </div>
            <div class="ticket-container">
                <div class="ticket-item" :key="`ticket-${ticket.id}`" v-for="ticket in scenicSpotTicketList">
                    <div class="icon">

                        <div style="display: flex;justify-content: center;">
                            <el-icon style="font-size: 50px;color: rgb(61, 190, 113);">
                                <ticket />
                            </el-icon>
                        </div>

                        <div style="margin-bottom: 10px;display: flex;align-items: center;justify-content: center;">
                            <el-tag type="warning">门票状态：{{ ticket.status === 1 ? '暂停预约' : '正常' }}</el-tag>
                        </div>

                        <div style="display: flex;justify-content: center;gap: 10px;align-items: center;">
                            <div class="fun-btn" @click="handleBuyTicket(ticket)">
                                <el-icon>
                                    <connection />
                                </el-icon>
                                立即买票
                            </div>
                        </div>
                    </div>
                    <div class="content">
                        <div class="detail" v-html="ticket.detail">
                        </div>
                        <div style="margin-top: 10px;">
                            总数量：
                            <span style="font-size: 20px;font-weight: 600;">{{ ticket.totalNumber }}</span>
                            张，单价：
                            <span style="font-size: 20px;font-weight: 600;">{{ ticket.price }}</span>
                            元/张
                        </div>
                        <div style="margin-block: 10px;">
                            <el-tag type="warning">有效期：{{ ticket.startDate }}至{{ ticket.endDate }}</el-tag>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 景点门票信息操作弹窗 -->
        <el-dialog title="确定购票" v-model="dialogVisible" width="600px" :close-on-click-modal="false">

            <div
                style="margin-bottom: 20px;padding: 10px 40px;box-sizing: border-box;background-color: rgb(246,246,246);margin-inline: 20px;border-radius: 6px;border: 1px solid rgb(232,232,232);">

                <div>
                    {{ scenicSpotTicket.detail }}
                </div>
                <div style="margin-block: 10px;font-weight: 600;">
                    有效期：{{ scenicSpotTicket.startDate }}至{{ scenicSpotTicket.endDate }}
                </div>
                <div>
                    <div style="margin-top: 10px;">
                        总数量：
                        <span style="font-size: 20px;font-weight: 600;color: rgb(255, 138, 23);">{{
                            scenicSpotTicket.totalNumber }}</span>
                        张，单价：
                        <span style="font-size: 20px;font-weight: 600;color: rgb(255, 138, 23);">{{
                            scenicSpotTicket.price }}</span>
                        元/张
                    </div>
                </div>
            </div>
            <el-form :model="scenicSpotTicketOrder" label-width="120px" ref="formRef">

                <el-form-item label="联系人" prop="concatUsername">
                    <el-input style="width: 90%;" type="text" placeholder="联系人"
                        v-model="scenicSpotTicketOrder.concatUsername"></el-input>
                </el-form-item>

                <el-form-item label="联系电话" prop="concatPhone">
                    <el-input style="width: 90%;" type="text" placeholder="联系电话"
                        v-model="scenicSpotTicketOrder.concatPhone"></el-input>
                </el-form-item>

                <el-form-item label="身份证号" prop="idcard">
                    <el-input style="width: 90%;" type="text" placeholder="身份证号"
                        v-model="scenicSpotTicketOrder.idcard"></el-input>
                </el-form-item>

                <el-form-item label="购买数量" prop="buyNumber">
                    <el-input-number v-model="scenicSpotTicketOrder.buyNumber" :min="1" :max="20"
                        label="购买数量"></el-input-number>
                </el-form-item>

            </el-form>

            <div style="padding: 10px 50px;font-size: 16px;">
                共需花费
                <span style="font-size: 30px;font-weight: 600;color: rgb(255, 138, 23);">
                    {{ totalBuyPrice }}
                </span>
                元
            </div>

            <template #footer>
                <div class="footer-container">
                    <div>
                        <div class="cancel" @click="handleCancel">取消</div>
                        <div class="fun-btn" @click="handleConfirm">我确定创建订单</div>
                    </div>
                </div>
            </template>
        </el-dialog>

    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from "vue"
import { ElMessage, type FormInstance, ElNotification } from "element-plus"
import { getDetailApi } from "@/api/scenic-spot-api"
import type { ScenicSpotVO } from "@/types/scenic-spot.ts"
import type { ScenicSpotTicket, ScenicSpotTicketQueryDTO } from "@/types/scenic-spot-ticket"
import {
    queryScenicSpotTicketApi
} from "@/api/scenic-spot-ticket-api"
import { useRouter } from "vue-router"

import type { ScenicSpotTicketOrder } from "@/types/scenic-spot-ticket-order"

import {
    addScenicSpotTicketOrdersApi
} from "@/api/scenic-spot-ticket-orders-api"


// 响应式数据
const scenicSpotDetail = reactive<Partial<ScenicSpotVO>>({})
const isLoading = ref(false)
const formRef = ref<FormInstance>()
const isCreateModel = ref(true)
const dialogVisible = ref(false)

const router = useRouter()

const totalBuyPrice = computed(() => {
    const price = scenicSpotTicket?.price ?? 0
    const quantity = scenicSpotTicketOrder?.buyNumber ?? 1
    const result = price * quantity
    return parseFloat(result.toFixed(2))  // 转为数字类型
})

const props = defineProps({
    scenicSpotId: {
        type: Number,
        default: '',
        required: true
    },
})

// 定义景点门票订单数据
const scenicSpotTicketOrder = reactive<Partial<ScenicSpotTicketOrder>>({
    buyNumber: 1
})

// 定义景点门票数据
const scenicSpotTicket = reactive<ScenicSpotTicket>({
    id: null,
    scenicId: null,
    totalNumber: null,
    price: null,
    detail: '',
    startDate: null,
    endDate: null,
    status: null
})

// 定义景点门票查询参数数据
const scenicSpotRouteItemQueryDTO = reactive<ScenicSpotTicketQueryDTO>({})

const scenicSpotTicketList = ref<ScenicSpotTicket[]>([])

const handleCancel = () => {
    resetForm()
}

const resetForm = () => {
    dialogVisible.value = false // 关闭操作弹窗
    Object.assign(scenicSpotTicket, {
        id: null,
        scenicId: null,
        totalNumber: null,
        price: null,
        detail: '',
        startDate: null,
        endDate: null,
        status: null,
        createTime: null
    })
    Object.assign(scenicSpotTicketOrder, {
        id: null,
        concatUsername: '',
        concatPhone: '',
        Idcard: '',
        buyNumber: 1
    })
}

const handleBuyTicket = (ticket: ScenicSpotTicket) => {
    dialogVisible.value = true
    isCreateModel.value = false
    Object.assign(scenicSpotTicket, { ...ticket })
}


// 查询景点门票
const queryScenicSpotTickets = async () => {
    try {
        scenicSpotRouteItemQueryDTO.scenicSpotId = props.scenicSpotId
        const { data } = await queryScenicSpotTicketApi(scenicSpotRouteItemQueryDTO)
        scenicSpotTicketList.value = data.map((ticket: ScenicSpotTicket) => {
            return {
                switchStatus: ticket.status === 2,
                ...ticket,
            }
        })
        console.log(JSON.stringify(scenicSpotTicketList.value));


    } catch (error: any) {
        console.error("景点门票查询异常：", error)
        ElMessage.error(`景点门票查询异常：${error}`)
    }
}

// 景点门票新增
const handleConfirm = async () => {
    try {
        if (!scenicSpotTicketOrder.concatUsername) {
            ElMessage.warning('联系人必填')
            return
        }
        if (!scenicSpotTicketOrder.concatPhone) {
            ElMessage.warning('联系电话必填')
            return
        }
        if (!scenicSpotTicketOrder.idcard) {
            ElMessage.warning('请填写身份证')
            return
        }
        if (!scenicSpotTicketOrder.buyNumber) {
            ElMessage.warning('请设置有效购票数')
            return
        }
        // 设置关联的景点门票ID
        scenicSpotTicketOrder.scenicSpotTicketId = scenicSpotTicket.id
        await addScenicSpotTicketOrdersApi(scenicSpotTicketOrder as any)
        // 提示操作成功
        ElNotification.success({
            title: '订单创建成功',
            message: '创建成功',
            duration: 1500,
            position: 'bottom-right'
        })
        // 重置响应式数据
        resetForm()
        // 跳转到景点门票列表
        router.push('/user/scenic-spot-order-ticket-list');
    } catch (error: any) {
        console.error('景点门票订单创建异常', error)
        ElMessage.warning(`${error.message}`)
    }
}

// 获取景点详情
const getScenicSpotDetail = async (id: number | null) => {
    if (!id) {
        console.error("景点ID为空")
        return
    }

    isLoading.value = true
    try {
        const { data } = await getDetailApi(id)
        // 清空原有数据
        Object.keys(scenicSpotDetail).forEach(key => {
            delete scenicSpotDetail[key as keyof ScenicSpotVO]
        })
        // 赋值新数据
        Object.assign(scenicSpotDetail, data)
    } catch (error: any) {
        console.error("查询景点详情异常：", error)
        // 可以添加错误提示
    } finally {
        isLoading.value = false
    }
}


onMounted(async () => {
    if (props.scenicSpotId) {
        await getScenicSpotDetail(props.scenicSpotId)
        await queryScenicSpotTickets()
    }
})
</script>

<style lang="scss" scoped>
.scenic-route-detail-container {
    max-width: 800px;
}

.cancel,
.fun-btn {
    padding: 2px 10px;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.5s ease;
}

.cancel {
    border: 1px solid rgb(51, 51, 51);

    &:hover {
        border: 1px solid rgb(31, 31, 31);
    }
}

.fun-btn {
    background-color: rgb(51, 51, 51);
    color: rgb(255, 255, 255);
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;

    &:hover {
        background-color: rgb(31, 31, 31);
    }
}

.ticket-container {
    // display: grid;
    // grid-template-columns: repeat(2, 1fr);
    // gap: 20px;


    .ticket-item {
        display: flex;
        align-items: center;
        // gap: 20px;
        // box-shadow: 0 4px 8px rgb(240, 240, 240);
        background-color: rgb(250, 251, 252);
        border: 1px solid rgb(240, 240, 240);
        margin-bottom: 10px;
        border-radius: 10px;
        padding: 10px;
        box-sizing: border-box;
        cursor: pointer;
        transition: all 0.5s ease;

        .icon {
            min-width: 150px;
        }

        .content {
            .detail {
                font-size: 14px;
                color: rgb(134, 134, 134);
            }
        }
    }
}

.footer-container {
    display: flex;
    justify-content: center;
    align-items: center;

    div {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 10px;

        .cancel,
        .fun-btn {
            padding: 6px 20px;
            border-radius: 20px;
            cursor: pointer;
            transition: all 0.5s ease;
        }

        .cancel {
            border: 1px solid rgb(51, 51, 51);

            &:hover {
                border: 1px solid rgb(31, 31, 31);
            }
        }

        .fun-btn {
            background-color: rgb(51, 51, 51);
            color: rgb(255, 255, 255);

            &:hover {
                background-color: rgb(31, 31, 31);
            }
        }
    }
}

.base-scenic-info {
    display: flex;
    padding: 30px 20px;
    box-sizing: border-box;
    align-items: center;
    gap: 20px;

    .content {

        .add {
            background-color: rgb(51, 51, 51);
            color: rgb(255, 255, 255);
            display: inline-block;
            padding: 6px 20px;
            border-radius: 20px;
            cursor: pointer;

            &:hover {
                background-color: rgb(31, 31, 31);
            }
        }

        img {
            width: 250px;
            aspect-ratio: 3/2;
            border-radius: 20px;
        }

        .title {
            font-size: 26px;
            margin-bottom: 10px;
        }

        .summary {
            color: rgb(120, 120, 120);
            margin-bottom: 10px;
        }
    }
}
</style>