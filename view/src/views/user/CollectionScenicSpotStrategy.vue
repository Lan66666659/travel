<template>
    <div class="scenic-spot-container">
        <div class="search-condition-container">
            <el-collapse v-model="activeName" accordion>
                <el-collapse-item :title="`我收藏的景点攻略${totalPageCount}篇（可展开此进行查询）`" name="1">
                    <!-- 景点攻略名称 -->
                    <div>
                        <div class="form-label">攻略标题</div>
                        <el-input style="max-width: 300px;" v-model="scenicSpotStrategyQueryDTO.title"
                            placeholder="标题"></el-input>
                    </div>
                    <!-- 录入时间 -->
                    <div>
                        <div class="form-label">发布开始时间-发布结束时间</div>
                        <el-date-picker style="max-width: 300px;" v-model="dateRanges" type="daterange"
                            range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期">
                        </el-date-picker>
                    </div>
                    <!-- 搜索按钮区域 -->
                    <div class="search-operation-container">
                        <el-button @click="searchScenicSpotStraegyList" type="primary" round>搜索</el-button>
                        <el-button plain round @click="resetCondition">重置</el-button>
                    </div>
                </el-collapse-item>
            </el-collapse>
        </div>
        <div v-if="!scenicSpotStrategyListVO.length">
            <el-empty description="暂无收藏景点攻略"></el-empty>
        </div>
        <!-- 景点攻略列表信息 -->
        <div class="scenic-list-container">

            <div class="scenic-spot-item" :key="`scenic-spot-item-${scenicSpotStategy.id}`"
                v-for="scenicSpotStategy in scenicSpotStrategyListVO">
                <img class="cover" :src="scenicSpotStategy.cover" alt="" srcset="">

                <div class="title">{{ scenicSpotStategy.title }}</div>
                <div class="btn-group">
                    <div class="block" @click="goDetail(scenicSpotStategy.id as any)">
                        <el-icon>
                            <View />
                        </el-icon>
                        <span>
                            查看详情
                        </span>
                    </div>
                    <div class="block" @click="cannelCollection(scenicSpotStategy)">
                        <span>
                            取消收藏
                        </span>
                    </div>
                </div>
                <div class="region">
                    <el-icon>
                        <Open />
                    </el-icon>

                    <span>{{ scenicSpotStategy.createTime }}</span>
                </div>
                <div class="summary">
                    {{ scenicSpotStategy.summary }}
                </div>
            </div>
        </div>
        <!-- 分页区域 -->
        <div v-if="scenicSpotStrategyListVO.length">
            <div class="pagination">
                <el-pagination v-model:current-page="scenicSpotStrategyQueryDTO.current"
                    v-model:page-size="scenicSpotStrategyQueryDTO.size" :page-sizes="[15, 30, 60]"
                    :total="totalPageCount" layout="total, sizes, prev, pager, next, jumper"
                    @size-change="handleSizeChange" @current-change="handleCurrentChange" />
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import {
    ElMessage
} from "element-plus"
import { useRouter } from "vue-router"
import { handleDateRange } from "@/utils/date"
import { ElMessageBox,ElNotification } from "element-plus"
import type {
    ScenicSpotStrategy,
    ScenicSpotStrategyQueryDTO,
    ScenicSpotStrategyListVO
} from "@/types/scenic-spot-strategy.ts"
import type {
    UserActionOperation
} from "@/types/user-action-operation.ts"
import {
    queryUserCollectionApi
} from "@/api/scenic-spot-strategy-api.ts"
import { collectionApi } from "@/api/user-action-operation-api"

// 响应式数据
const scenicSpotStrategyQueryDTO = reactive<ScenicSpotStrategyQueryDTO>({
    current: 1,
    size: 15,
    title: '',
    scenicSpotId: null
})

const activeName = ref('1')
const dateRanges = ref<Date[]>([])
const contentModule = ref("SCENIC_SPOT_STRATEGY")

const scenicSpotStrategyListVO = ref<ScenicSpotStrategyListVO[]>([]) // 景点攻略列表数据
const totalPageCount = ref<number | undefined>(0) // 总数据条数

const router = useRouter()

const resetCondition = async () => {
    scenicSpotStrategyQueryDTO.title = ''
    scenicSpotStrategyQueryDTO.startTime = null
    scenicSpotStrategyQueryDTO.endTime = null
    dateRanges.value = []
    await fetchScenicSpotStrategy()
}

// 跳转详情页
const goDetail = (id: number) => {
    window.open(`/scenic-spot-strategy-detail?scenicSpotStrategyId=${id}`);
}


// 搜索事件
const searchScenicSpotStraegyList = async () => {
    const dates = handleDateRange(dateRanges.value)
    // 如果用户选中了时间，则进行时间的格式处理
    if (dates) {
        scenicSpotStrategyQueryDTO.startTime = dates.start
        scenicSpotStrategyQueryDTO.endTime = dates.end
    }
    await fetchScenicSpotStrategy()
}

// 取消收藏
const cannelCollection = async (scenicSpotStrategy: ScenicSpotStrategy) => {
    try {
        const response = await ElMessageBox.confirm(
            `确定要取消收藏"【${scenicSpotStrategy.title}】"这篇景点攻略吗？`,
            '操作确认',
            {
                confirmButtonText: '确定取消收藏',
                cancelButtonText: '取消',
                type: 'warning',
                center: true
            }
        )
        if (response === 'confirm') {
            try {
                if (!scenicSpotStrategy.id) {
                    return
                }
                const userActionOperation: UserActionOperation = {
                    contentModule: contentModule.value,
                    contentId: scenicSpotStrategy.id
                }
                await collectionApi(userActionOperation)

                await fetchScenicSpotStrategy()

                ElNotification.success({
                    title: '用户操作',
                    message: '取消收藏成功',
                    duration: 1500,
                    position: 'top-right'
                })

            } catch (error: any) {
                console.error("取消收藏操作异常:", error)
                ElNotification.warning({
                    title: '用户操作',
                    message: error,
                    duration: 1500,
                    position: 'top-right'
                })
            }
        }
    } catch (error) {
        // 用户取消删除
        console.log('取消删除')
    }
}

const handleSizeChange = async (size: number) => {
    scenicSpotStrategyQueryDTO.size = size
    scenicSpotStrategyQueryDTO.current = 1
    await fetchScenicSpotStrategy()
}

const handleCurrentChange = async (current: number) => {
    scenicSpotStrategyQueryDTO.current = current
    await fetchScenicSpotStrategy()
}

const fetchScenicSpotStrategy = async () => {
    try {
        const { data, total } = await queryUserCollectionApi(scenicSpotStrategyQueryDTO)
        scenicSpotStrategyListVO.value = data
        totalPageCount.value = total
    } catch (error: any) {
        ElMessage.error('景点攻略攻略查询异常：', error)
    }
}

onMounted(async () => {
    // await queryRegionList();
    await fetchScenicSpotStrategy();
})

</script>
<style lang="scss" scoped>
$primary-color: #409eff;
$border-color: #e4e7ed;
$transition-base: all 0.3s ease;


.scenic-spot-container {
    padding: 20px 0;
    box-sizing: border-box;
}

// .search-condition-container {
//     padding: 10px;
// }

.search-operation-container {
    margin-block: 20px;
}

:deep(.el-collapse) {
    border-top: none;
    border-bottom: none;
}

:deep(.el-collapse-item__title) {
    font-size: 22px;
}

:deep(.el-collapse-item__header) {
    border-bottom: none;
}

.form-label {
    font-size: 16px;
    color: rgb(99, 99, 99);
    margin-block: 16px;
}

.region-group,
.category-group {

    .region-level,
    .category-level {
        margin-bottom: 24px;


        .radio-group {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;

            .radio-item {
                --el-radio-font-size: 14px;
                --el-radio-checked-color: $primary-color;
                padding: 4px 12px;
                border: 1px solid $border-color;
                border-radius: 8px;
                cursor: pointer;
                transition: $transition-base;

                &:hover {
                    border-color: $primary-color;
                    background-color: rgba(64, 158, 255, 0.05);
                }

                .el-radio__input:checked+.el-radio__label {
                    color: $primary-color;
                    font-weight: 500;
                }
            }
        }
    }
}

.btn-group {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-block: 10px;

    .block {
        display: flex;
        justify-content: left;
        align-items: center;
        cursor: pointer;
        color: rgb(31, 31, 31);
        gap: 2px;

        &:hover {
            text-decoration: underline;
        }
    }

    .block:last-child {
        background-color: rgb(51, 51, 51);
        color: rgb(255, 255, 255);
        padding: 4px 10px;
        border-radius: 20px;

        &:hover {
            text-decoration: none;
            background-color: rgb(31, 31, 31);
        }
    }
}

.pagination {
    padding-block: 20px;
}

.scenic-list-container {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    grid-gap: 20px;

    .scenic-spot-item {
        // 父相子绝
        position: relative;

        padding: 10px;
        box-sizing: border-box;
        cursor: pointer;
        transition: all 0.5s ease;

        &:hover {
            box-shadow: 0 4px 6px rgb(240, 240, 240);
            border-radius: 10px;
        }

        .cover {
            width: 100%;
            object-fit: cover;
            aspect-ratio: 4/3;
            border-radius: 10px;
            cursor: pointer;
            transition: all .5s ease;

            &:hover {
                scale: 1.05;
            }
        }

        .title {
            max-width: 200px;
            font-size: 20px;
            color: rgb(31, 31, 31);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .summary {
            position: absolute;
            right: 0;
            top: 30px;
            width: 100%;
            text-align: start;
            color: rgb(255, 255, 255);
            padding: 20px;
            box-sizing: border-box;
            font-size: 12px;

            /* 核心：三行省略关键属性 */
            display: -webkit-box;
            /* 弹性盒模型 */
            -webkit-box-orient: vertical;
            /* 垂直排列 */
            -webkit-line-clamp: 2;
            /* 限制显示行数 */
            overflow: hidden;
            /* 超出部分隐藏 */
            text-overflow: ellipsis;
            /* 省略号效果（兼容兜底） */
            /* 可选：增加行高，提升可读性 */
            line-height: 1.7;
            /* 防止绝对定位元素被遮挡 */
            // z-index: 10;
        }

        .region {
            position: absolute;
            right: 16px;
            top: 16px;
            background-color: rgba(0, 0, 0, 0.7);
            color: rgb(255, 255, 255);
            font-size: 12px;
            padding: 4px 12px;
            border-radius: 20px;
            display: flex;
            justify-content: left;
            align-items: center;
            cursor: pointer;
            gap: 6px;

            .point {
                width: 4px;
                height: 4px;
                border-radius: 50%;
                background-color: rgb(255, 255, 255);
            }

            &:hover {
                background-color: rgba(0, 0, 0, 0.8);
            }
        }
    }
}

.top-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 0 26px 0;
    background: #fff;
    border-radius: 6px;

    .nav-left,
    .nav-right {
        display: flex;
        align-items: center;
        gap: 12px;
    }
}
</style>