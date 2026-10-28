<template>
    <div class="scenic-spot-top-container">
        <!-- 景点推荐 -->
        <div>
            <ScenicSpotItem key="recommend-scenic-spot" @search="goScenicSpotSearch" @view-detail="goDetail" title="景点推荐"
                subTitle="大家爱看，有你爱看" :scenic-spot-list="recommendScenicSpotList" />
        </div>
        <!-- 最热景点推荐 -->
        <div>
            <ScenicSpotItem key="hot-scenic-spot" @search="goScenicSpotSearch" @view-detail="goDetail" title="最热景点"
                subTitle="有你想看，大家在看" :scenic-spot-list="hotScenicSpotList" />
        </div>
        <!-- 最新景点 -->
        <div>
            <ScenicSpotItem key="last-scenic-spot" @search="goScenicSpotSearch" @view-detail="goDetail" title="最新景点"
                subTitle="精彩人生，旅游生活" :scenic-spot-list="lastScenicSpotList" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import ScenicSpotItem from "@/components/ScenicSpotItem.vue"
import {
    ElMessage
} from "element-plus"
import { useRouter } from "vue-router"
import type { ScenicSpotListVO, ScenicSpotQueryDTO } from "@/types/scenic-spot"
import type { Region, RegionQueryDTO } from "@/types/region"
import {
    queryScenicSpotApi,
    queryRecommendApi
} from "@/api/scenic-spot-api"
import { queryListApi } from "@/api/region-api"
import { handleDateRange } from "@/utils/date"


// 响应式数据
const scenicSpotQueryDTO = reactive<ScenicSpotQueryDTO>({
    current: 1,
    size: 5,
    title: '',
    regionIds: [],
    startTime: null,
    endTime: null
})

const lastScenicSpotList = ref<ScenicSpotListVO[]>([])
const hotScenicSpotList = ref<ScenicSpotListVO[]>([])
const recommendScenicSpotList = ref<ScenicSpotListVO[]>([])
const recommendScenicSpotCount = ref(4) // 默认推荐4条

const activeName = ref('1')
const regionProvinceId = ref<null | number>(null) // 父地区ID
const childrenRegionId = ref<null | number>(null) // 子地区ID
const childrenRegionList = ref<Region[]>([])
const regionList = ref<Region[]>([])
const dateRanges = ref<Date[]>([])
const regionQueryDTO = reactive<RegionQueryDTO>({
    parentId: -1
})

const contentModule = ref("SCENIC_SPOT")

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
    await fetchLastScenicSpotList()
}

// 跳转详情页
const goDetail = (id: number) => {
    window.open(`/scenic-spot-info?scenicSpotId=${id}`);
}

const goScenicSpotSearch = (id: number) => {
    window.open(`/home-scenic-spot-list`);
}

// 搜索事件
const searchScenicSpotList = async () => {
    if (regionProvinceId.value === -2) {
        scenicSpotQueryDTO.regionIds = []
        await fetchLastScenicSpotList()
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
    await fetchLastScenicSpotList()

}


const queryRegionList = async () => {
    try {
        const { data } = await queryListApi(regionQueryDTO)
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

        const { data } = await queryListApi({
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
    await fetchLastScenicSpotList()
}

const handleCurrentChange = async (current: number) => {
    scenicSpotQueryDTO.current = current
    await fetchLastScenicSpotList()
}

const handleSearch = async (keyword: string) => {
    scenicSpotQueryDTO.title = keyword
    scenicSpotQueryDTO.current = 1
    await fetchLastScenicSpotList()
}

const fetchLastScenicSpotList = async () => {
    try {
        const { data } = await queryScenicSpotApi(scenicSpotQueryDTO)
        lastScenicSpotList.value = data
    } catch (error: any) {
        throw new Error(`最新景点数据查询异常：${error}`)
    }
}

const fetchHotTopScenicSpotList = async () => {
    try {
        const scenicSpotQueryDTOHot = {
            size: 5,
            current: 1,
            hotRule: true
        }
        const { data } = await queryScenicSpotApi(scenicSpotQueryDTOHot as ScenicSpotQueryDTO)
        hotScenicSpotList.value = data
    } catch (error: any) {
        throw new Error(`最热景点数据查询异常：${error}`)
    }
}

const fetchRecommendScenicSpotList = async () => {
    try {
        const { data } = await queryRecommendApi(recommendScenicSpotCount.value)
        recommendScenicSpotList.value = data
    } catch (error: any) {
        throw new Error(`查询推荐的景点数据异常：${error}`)
    }
}

onMounted(async () => {
    await fetchLastScenicSpotList()
    await fetchHotTopScenicSpotList()
    await fetchRecommendScenicSpotList()
})

</script>
<style lang="scss" scoped></style>