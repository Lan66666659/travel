<template>
    <div class="scenic-spot-container">
        <div class="form-label" style="font-size: 26px;padding-inline: 160px;">共收录景点攻略
            <span style="font-size: 40px;font-weight: 600;">{{ totalPageCount }}</span>
            篇
        </div>
        <div class="search-condition-container">
            <!-- 景点攻略名称 -->
            <div style="margin-bottom: 20px;">
                <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">攻略标题</div>
                <el-input style="max-width: 300px;" v-model="scenicSpotStrategyQueryDTO.title"
                    placeholder="标题"></el-input>
            </div>
            <!-- 录入时间 -->
            <div>
                <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">发布开始时间-发布结束时间</div>
                <el-date-picker style="max-width: 300px;" v-model="dateRanges" type="daterange" range-separator="至"
                    start-placeholder="开始日期" end-placeholder="结束日期">
                </el-date-picker>
            </div>
            <!-- 搜索按钮区域 -->
            <div class="search-operation-container" style="margin-top: 30px;">
                <el-button @click="searchScenicSpotStraegyList" icon="Search" type="primary" round>立即搜索景点攻略</el-button>
                <el-button plain round @click="resetCondition" icon="BrushFilled">条件重置</el-button>
            </div>

        </div>
        <div v-if="!scenicSpotStrategyListVO.length">
            <el-empty description="暂无景点攻略数据"></el-empty>
        </div>
        <div class="strategy-container">
            <!-- 景点攻略进行渲染 -->
            <div class="item-strategy" :key="`strategy-${strategy.id}-${strategy.title}`"
                v-for="strategy in scenicSpotStrategyListVO">
                <div class="cover">
                    <el-tooltip class="item" effect="dark" :content="strategy.summary" placement="bottom">
                        <img :src="strategy.cover" alt="" srcset="">
                    </el-tooltip>
                </div>
                <div class="title" @click="goDetail(strategy.id)">{{ strategy.title }}</div>
                <div class="user-info">
                    <img :src="strategy.avatar" alt="" srcset="">
                    <span class="username">{{ strategy.username }}</span>
                </div>
            </div>
        </div>
        <!-- 分页区域 -->
        <div v-if="scenicSpotStrategyListVO.length">
            <div class="pagination">
                <el-pagination v-model:current-page="scenicSpotStrategyQueryDTO.current"
                    v-model:page-size="scenicSpotStrategyQueryDTO.size" :page-sizes="[12, 24]" :total="totalPageCount"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { handleDateRange } from "@/utils/date"
import type {
    ScenicSpotStrategyQueryDTO,
    ScenicSpotStrategyListVO
} from "@/types/scenic-spot-strategy.ts"
import {
    queryAllScenciSpotStrategyByViewerApi
} from "@/api/viewer-api"

// 响应式数据
const scenicSpotStrategyQueryDTO = reactive<ScenicSpotStrategyQueryDTO>({
    current: 1,
    size: 12,
    title: '',
    scenicSpotId: null
})

const dateRanges = ref<Date[]>([])

const scenicSpotStrategyListVO = ref<ScenicSpotStrategyListVO[]>([]) // 景点攻略列表数据
const totalPageCount = ref<number | undefined>(0) // 总数据条数

const resetCondition = async () => {
    scenicSpotStrategyQueryDTO.title = ''
    scenicSpotStrategyQueryDTO.startTime = null
    scenicSpotStrategyQueryDTO.endTime = null
    dateRanges.value = []
    await fetchScenicSpotStrategy()
}


// 跳转详情页
const goDetail = (id: number | undefined) => {
    if (!id) return
    window.open(`/viewer-scenic-spot-strategy-detail?scenicSpotStrategyId=${id}`);
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
        const { data, total } = await queryAllScenciSpotStrategyByViewerApi(scenicSpotStrategyQueryDTO)
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
.search-condition-container {
    padding: 10px 160px 20px 160px;
}

.pagination {
    padding: 20px;
    display: flex;
    justify-content: center;
}

.strategy-container {
    display: flex;
    flex-wrap: wrap;
    gap: 40px;
    padding-inline: 160px;
    padding-block: 20px;

    .item-strategy {
        width: calc(25% - 40px);
        position: relative;

        .title {
            font-size: 20px;
            display: -webkit-box;
            /* 使用弹性盒子布局 */
            -webkit-box-orient: vertical;
            /* 垂直排列 */
            -webkit-line-clamp: 2;
            /* 显示2行 */
            overflow: hidden;
            /* 隐藏溢出内容 */
            text-overflow: ellipsis;
            /* 显示省略号 */
            line-height: 1.4;
            /* 设置行高，优化显示 */
            cursor: pointer;
            color: rgb(65, 134, 174);

            &:hover {
                color: rgb(49, 111, 147);
            }
        }

        .cover {
            cursor: pointer;

            img {
                width: 100%;
                height: 166px;
                border-radius: 10px;
                transition: all 0.8s ease;

                &:hover {
                    scale: 1.05;
                }
            }
        }

        .user-info {
            position: absolute;
            top: 10px;
            left: 10px;
            display: flex;
            justify-content: left;
            align-items: center;
            gap: 6px;
            //background-color: rgb(22, 119, 255);
            background-color: rgba(0, 0, 0, 0.7);
            padding: 4px 10px;
            border-radius: 20px;

            img {
                width: 20px;
                height: 20px;
                border-radius: 50%;
            }

            .username {
                font-size: 12px;
                font-weight: 600;
                color: rgb(255, 255, 255);
            }
        }
    }
}
</style>