<template>
    <div class="scenic-spot-top-container">
        <!-- 最热景点攻略推荐 -->
        <div>
            <ScenicSpotStrategyItem key="hot-scenic-spot-strategy" @view="viewStrategy"
                @search="goScenicSpotStrategySearch" title="最热景点攻略" subTitle="有你想看，大家在看"
                :scenic-spot-strategy-list="hotScenicSpotStrategyList" />
        </div>
        <!-- 最新景点攻略 -->
        <div>
            <ScenicSpotStrategyItem @view="viewStrategy" key="last-scenic-spot-strategy"
                @search="goScenicSpotStrategySearch" title="最新景点攻略" subTitle="精彩人生，旅游生活"
                :scenic-spot-strategy-list="lastScenicSpotStrategyList" />
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from "vue"
import ScenicSpotStrategyItem from "@/components/ScenicSpotStrategyItem.vue"
import { useRouter } from "vue-router"
import type { ScenicSpotStrategyListVO, ScenicSpotStrategyQueryDTO } from "@/types/scenic-spot-strategy"
import {
    queryAllScenciSpotStrategyByViewerApi
} from "@/api/viewer-api"


// 响应式数据
const scenicSpotStrategyQueryDTO = reactive<ScenicSpotStrategyQueryDTO>({
    current: 1,
    size: 8,
})

const lastScenicSpotStrategyList = ref<ScenicSpotStrategyListVO[]>([])
const hotScenicSpotStrategyList = ref<ScenicSpotStrategyListVO[]>([])

const router = useRouter()


// 跳转详情页
const viewStrategy = (id: number) => {
    window.open(`/viewer-scenic-spot-strategy-detail?scenicSpotStrategyId=${id}`);
}

// 景点攻略搜索
const goScenicSpotStrategySearch = () => {
    window.open(`/viewer-scenic-spot-strategy-list`);
}

const fetchLastScenicStrategySpotList = async () => {
    try {
        const { data } = await queryAllScenciSpotStrategyByViewerApi(scenicSpotStrategyQueryDTO)
        lastScenicSpotStrategyList.value = data
    } catch (error: any) {
        throw new Error(`最新景点攻略数据查询异常：${error}`)
    }
}

const fetchHotTopScenicSpotStrategyList = async () => {
    try {
        const scenicSpotStrategyQueryDTOHot = {
            size: 8,
            current: 1,
            hotRule: true
        }
        const { data } = await queryAllScenciSpotStrategyByViewerApi(scenicSpotStrategyQueryDTOHot)
        hotScenicSpotStrategyList.value = data
    } catch (error: any) {
        throw new Error(`最热景点攻略数据查询异常：${error}`)
    }
}


onMounted(async () => {
    await fetchLastScenicStrategySpotList()
    await fetchHotTopScenicSpotStrategyList()
})

</script>
<style lang="scss" scoped></style>