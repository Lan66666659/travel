<template>
    <div class="scenic-spot-container">
        <div class="search-condition-container">
            <el-collapse v-model="activeName" accordion>
                <el-collapse-item :title="`景点${totalPageCount}处（可展开此进行查询）`" name="1">
                    <!-- 地区信息 -->
                    <div class="region-group">
                        <div class="region-level">
                            <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">地区</div>
                            <el-radio-group v-model="regionProvinceId" @change="handleChangeProvince"
                                class="radio-group">
                                <el-radio v-for="region in regionList" :key="region.id" :label="region.id"
                                    class="radio-item">
                                    {{ region.name }}
                                </el-radio>
                            </el-radio-group>
                        </div>

                        <div class="region-level" v-if="childrenRegionList.length">
                            <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">下级地区</div>
                            <el-radio-group v-model="childrenRegionId" class="radio-group">
                                <el-radio v-for="region in childrenRegionList" :key="region.id" :label="region.id"
                                    class="radio-item">
                                    {{ region.name }}
                                </el-radio>
                            </el-radio-group>
                        </div>
                    </div>
                    <!-- 景点名称 -->
                    <div>
                        <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">景点名称</div>
                        <el-input style="max-width: 300px;" v-model="scenicSpotQueryDTO.title"
                            placeholder="景点名称"></el-input>
                    </div>
                    <div>
                        <div class="form-label" style="font-size: 22px;margin-bottom: 10px;">类别</div>
                        <el-input style="max-width: 300px;" v-model="scenicSpotQueryDTO.categoryKey"
                            placeholder="类别名"></el-input>
                    </div>
                    <!-- 搜索按钮区域 -->
                    <div class="search-operation-container">
                        <el-button @click="searchScenicSpotList" icon="Search" type="primary" round>立即搜索景点</el-button>
                        <el-button plain round @click="resetCondition" icon="BrushFilled">条件重置</el-button>
                    </div>
                </el-collapse-item>
            </el-collapse>
        </div>
        <div v-if="!scenicSpotListVOS.length">
            <el-empty description="暂无景点数据"></el-empty>
        </div>
        <!-- 景点列表信息 -->


        <div class="strategy-container">
            <!-- 景点攻略进行渲染 -->
            <div class="item-strategy" :key="`scenic-${scenic.id}-${scenic.title}`" v-for="scenic in scenicSpotListVOS">
                <div class="cover">
                    <el-tooltip class="item" effect="dark" :content="scenic.summary" placement="bottom">
                        <img :src="scenic.cover" alt="" srcset="">
                    </el-tooltip>
                </div>
                <div class="title" @click="goDetail(scenic.id)">{{ scenic.title }}</div>
                <div class="user-info">
                    <el-icon>
                        <location-filled />
                    </el-icon>
                    <span>{{ scenic.provinceName }}</span>
                    <span class="point"></span>
                    <span>{{ scenic.cityName }}</span>
                </div>
            </div>
        </div>

        <!-- 分页区域 -->
        <div v-if="scenicSpotListVOS.length">
            <div class="pagination">
                <el-pagination v-model:current-page="scenicSpotQueryDTO.current"
                    v-model:page-size="scenicSpotQueryDTO.size" :page-sizes="[15, 30, 60]" :total="totalPageCount"
                    layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
                    @current-change="handleCurrentChange" />
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
import type { ScenicSpotListVO, ScenicSpotQueryDTO } from "@/types/scenic-spot"
import type { Region, RegionQueryDTO } from "@/types/region"
import {
    queryScenicSpotByViewerApi,
    queryRegionListByViewerApi
} from "@/api/viewer-api"
import { handleDateRange } from "@/utils/date"


// 响应式数据
const scenicSpotQueryDTO = reactive<ScenicSpotQueryDTO>({
    current: 1,
    size: 15,
    title: '',
    regionIds: [],
    startTime: null,
    endTime: null
})

const activeName = ref('1')
const regionProvinceId = ref<null | number>(null) // 父地区ID
const childrenRegionId = ref<null | number>(null) // 子地区ID
const childrenRegionList = ref<Region[]>([])
const regionList = ref<Region[]>([])
const dateRanges = ref<Date[]>([])
const regionQueryDTO = reactive<RegionQueryDTO>({
    parentId: -1
})

const scenicSpotListVOS = ref<ScenicSpotListVO[]>([]) // 景点列表数据
const totalPageCount = ref<number | undefined>(0) // 总数据条数

const router = useRouter()

const resetCondition = async () => {
    scenicSpotQueryDTO.regionIds = []
    scenicSpotQueryDTO.title = ''
    regionProvinceId.value = -2
    childrenRegionId.value = -3
    scenicSpotQueryDTO.startTime = null
    scenicSpotQueryDTO.endTime = null
    dateRanges.value = []
    childrenRegionList.value = []
    scenicSpotQueryDTO.categoryKey = ''
    await fetchScenicSpot()
}

// 跳转详情页
const goDetail = (id: number) => {
    window.open(`/viewer-scenic-spot-info?scenicSpotId=${id}`);
}

// 搜索事件
const searchScenicSpotList = async () => {
    if (regionProvinceId.value === -2) {
        scenicSpotQueryDTO.regionIds = []
        await fetchScenicSpot()
        return
    }
    // 处理地区信息
    if (regionProvinceId.value && regionProvinceId.value !== -2) {
        scenicSpotQueryDTO.regionIds = []
        if (childrenRegionId.value && childrenRegionId.value !== -3) {
            scenicSpotQueryDTO.regionIds?.push(
                regionProvinceId.value,
                childrenRegionId.value
            )
        } else { // 只查省份
            const regionIds = childrenRegionList.value
                .filter(region => region.id !== -3)
                .map(entity => entity.id)
            scenicSpotQueryDTO.regionIds?.push(
                ...regionIds as any
            )
        }

    }
    const dates = handleDateRange(dateRanges.value)
    if (dates) {
        scenicSpotQueryDTO.startTime = dates.start
        scenicSpotQueryDTO.endTime = dates.end
    }
    await fetchScenicSpot()

}


const queryRegionList = async () => {
    try {
        const { data } = await queryRegionListByViewerApi(regionQueryDTO)
        regionList.value = data
        // 加上不限制父地区的条件
        regionList.value.unshift(
            {
                id: -2,
                name: '不限制地区'
            }
        )
    } catch (error: any) {
        ElMessage.error('查询地区信息异常：', error)
    }
}

const handleChangeProvince = async () => {
    try {

        console.log(regionProvinceId.value);


        if (regionProvinceId.value === -2) { // 证明选中的是不限制地区，子地区不必显示
            childrenRegionList.value = []
            return
        }

        const { data } = await queryRegionListByViewerApi({
            parentId: regionProvinceId.value
        })
        childrenRegionList.value = data
        // 加上不限制子地区的条件
        childrenRegionList.value.unshift(
            {
                id: -3,
                name: '不限制子地区'
            }
        )
    } catch (error: any) {
        ElMessage.error('查询子地区信息异常：', error)
    }
}

const handleSizeChange = async (size: number) => {
    scenicSpotQueryDTO.size = size
    scenicSpotQueryDTO.current = 1
    await fetchScenicSpot()
}

const handleCurrentChange = async (current: number) => {
    scenicSpotQueryDTO.current = current
    await fetchScenicSpot()
}

const fetchScenicSpot = async () => {
    try {
        const { data, total } = await queryScenicSpotByViewerApi(scenicSpotQueryDTO)
        scenicSpotListVOS.value = data
        totalPageCount.value = total
    } catch (error: any) {
        ElMessage.error('景点查询异常：', error)
    }
}

onMounted(async () => {
    await queryRegionList();
    await fetchScenicSpot();
})

</script>
<style lang="scss" scoped>
$primary-color: #409eff;
$border-color: #e4e7ed;
$transition-base: all 0.3s ease;


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
            font-size: 12px;
            font-weight: 600;
            color: rgb(255, 255, 255);
        }
    }
}

.search-condition-container {
    padding: 10px 160px;
}

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
    display: flex;
    justify-content: center;
    padding-block: 30px;
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