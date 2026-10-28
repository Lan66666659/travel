<template>
    <div class="scenic-spot-top-container">
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
import type { ScenicSpotListVO, ScenicSpotQueryDTO } from "@/types/scenic-spot"
import {
    queryScenicSpotByViewerApi
} from "@/api/viewer-api"

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

// 跳转详情页
const goDetail = (id: number) => {
    window.open(`/viewer-scenic-spot-info?scenicSpotId=${id}`);
}

const goScenicSpotSearch = (id: number) => {
    window.open(`/viewer-scenic-spot-search-list`);
}

const fetchLastScenicSpotList = async () => {
    try {
        const { data } = await queryScenicSpotByViewerApi(scenicSpotQueryDTO)
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
        const { data } = await queryScenicSpotByViewerApi(scenicSpotQueryDTOHot as ScenicSpotQueryDTO)
        hotScenicSpotList.value = data
    } catch (error: any) {
        throw new Error(`最热景点数据查询异常：${error}`)
    }
}


onMounted(async () => {
    await fetchLastScenicSpotList()
    await fetchHotTopScenicSpotList()
})

</script>
<style lang="scss" scoped></style>