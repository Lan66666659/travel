<template>
    <div class="scenic-detail-container">

        <div
            style="padding-inline: 300px;padding-block: 10px;position: sticky;top: 0px;z-index: 100;background-color: rgb(255,255,255);">
            <div style="margin-block: 20px;display: flex;justify-content: left;align-items: center;gap: 10px;">
                <img style="width: 40px;height: 40px;" src="/logo.png" alt="">
                <div style="font-size: 32px;">海牛旅游网</div>
            </div>
            <div style="font-size: 32px;margin-block: 15px;font-weight: 600;">
                {{ scenicSpotDetail.title }}
            </div>
            <div
                style="color: rgb(148,148,148);margin-bottom: 20px;font-size: 18px;display: flex;justify-content: space-between;">
                <div>
                    <div style="display: flex;justify-content: left;align-items: center;gap: 4px;">
                        <el-icon>
                            <collection-tag />
                        </el-icon>

                        <span :key="`category-${category.id}`" v-for="category in categoryList">
                            {{ category.name }}
                            <span class="line"
                                style="display: inline-block;width: 4px;height: 4px;background-color: rgb(148,148,148);border-radius: 50%;"></span>
                        </span>

                        <span style="margin-left: 20px;">发布于{{ scenicSpotDetail.createTime }}</span>
                    </div>


                </div>

                <div style="display: flex;gap: 10px;">
                    <span>{{ scenicSpotDetail.viewCount }}人阅读</span>
                    <span>{{ scenicSpotDetail.upvoteCount }}人点赞</span>
                    <span>{{ scenicSpotDetail.collectionCount }}人收藏</span>
                </div>
            </div>
        </div>

        <!-- 行为操作区域 -->
        <div class="action-container">
            <div class="item" @click="collectionOperation">
                <div class="children">
                    <el-icon v-if="!collectionStatus" style="font-size: 22px;font-weight: 600;">
                        <star />
                    </el-icon>
                    <el-icon v-else style="font-size: 22px;font-weight: 600;">
                        <star-filled />
                    </el-icon>
                </div>
                <div class="children">
                    {{ collectionStatus ? '取消收藏' : '收藏' }}
                </div>
            </div>

            <div class="item" @click="upvoteOperation">
                <div class="children">
                    <el-icon style="font-size: 22px;font-weight: 600;">
                        <CollectionTag />
                    </el-icon>

                </div>
                <div class="children">
                    {{ upvoteStatus ? '取消点赞' : '点赞' }}
                </div>
            </div>
        </div>

        <div v-if="scenicSpotDetail.id" class="content">

            <div class="container">
                <!-- 摘要 -->
                <div class="summary">
                    {{ scenicSpotDetail.summary }}
                </div>
                <!-- 图册 -->
                <div class="brochure-container">
                    <div v-for="(brochure, index) in scenicSpotDetail.brochureList" :key="brochure.id"
                        class="brochure-item" @click="previewImage(brochure.coverUrl, index)">
                        <div class="brochure-image-wrapper">
                            <img :src="brochure.coverUrl" :alt="`图册图片 ${index + 1}`" class="brochure-image"
                                loading="lazy">
                            <div class="image-overlay">
                                <span class="preview-text">点击预览</span>
                            </div>
                            <div class="image-order">
                                第 {{ brochure.orderNumber }} 张
                            </div>
                        </div>
                    </div>
                </div>


            </div>

        </div>


        <div
            style="margin-top: 40px;margin-bottom: 20px;display: flex;justify-content: center;align-items: center;gap: 10px;">
            <div :style="{
                backgroundColor: tabIndex === tab.value ? 'rgb(244,245,246)' : '',
                fontWeight: tabIndex === tab.value ? '600' : ''
            }" class="tab-item" v-for="tab in tabs" @click="handleTabChange(tab)">
                {{ tab.name }}
            </div>
        </div>

        <!-- 详细内容 -->
        <div class="content-section" v-if="tabIndex === 1">
            <div class="content-wrapper" v-html="scenicSpotDetail.content"></div>
        </div>
        <!-- 用户评论 -->
        <div class="content-section" v-if="tabIndex === 2">
            <Comment v-if="userInfo" :currentUserId="Number(userInfo.id)" :userAvatar="String(userInfo.avatar)"
                :username="String(userInfo.username)" :contentId="Number(scenicSpotDetail.id)"
                :contentType="contentModule" />
        </div>
        <!-- 景点路线 -->
        <div v-if="tabIndex === 3">
            <UserScenicSpotRouteItem :scenic-spot-id="scenicSpotDetail.id" />
        </div>
        <!-- 景点攻略 -->
        <div class="content-section" v-if="tabIndex === 4">
            <!-- {{ scenicSpotStrategyListVO }} -->
            <div v-if="!scenicSpotStrategyListVO.length">
                <el-empty description="景点暂无关联的景点攻略数据"></el-empty>
            </div>
            <div v-else>
                <ScenicSpotWithStrategyItem key="hot-scenic-spot-strategy" @view="viewStrategy"
                    @search="goScenicSpotStrategySearch" title="与之相关的景点攻略" subTitle="真知灼见，看你想看"
                    :scenic-spot-strategy-list="scenicSpotStrategyListVO" />
            </div>
        </div>
        <!-- 门票 -->
        <div class="content-section" v-if="tabIndex === 5">
            <UserScenicSpotTickets :scenic-spot-id="scenicSpotDetail.id" />
        </div>
    </div>

    <!-- 图片预览模态框 -->
    <div v-if="showPreview" class="preview-modal" @click="closePreview">
        <div class="modal-content" @click.stop>
            <button class="modal-close" @click="closePreview">×</button>
            <img :src="currentPreviewImage" :alt="'预览图片'" class="preview-img" />
            <div class="modal-controls">
                <button class="nav-btn prev-btn" @click="prevImage" :disabled="currentPreviewIndex === 0">
                    ←
                </button>
                <span class="image-counter">
                    {{ currentPreviewIndex + 1 }} / {{ previewImages.length }}
                </span>
                <button class="nav-btn next-btn" @click="nextImage"
                    :disabled="currentPreviewIndex === previewImages.length - 1">
                    →
                </button>
            </div>
        </div>
    </div>

</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from "vue"
import ScenicSpotWithStrategyItem from "@/components/ScenicSpotWithStrategyItem.vue"
import UserScenicSpotRouteItem from "./UserScenicSpotRouteItem.vue"
import UserScenicSpotTickets from "./UserScenicSpotTickets.vue"
import { getUserInfo } from "@/utils/storage"
import Comment from "@/components/Comment.vue"
import { useRouter } from "vue-router"
import { onBeforeRouteLeave } from 'vue-router'
import { ElNotification, ElMessage } from "element-plus"
import { getDetailApi } from "@/api/scenic-spot-api"
import type { ScenicSpotVO } from "@/types/scenic-spot.ts"
import type { ScenicSpotStrategyListVO } from "@/types/scenic-spot-strategy"
import type { User } from "@/types/user"
import type {
    UserActionOperation,
    UserActionOperationQueryDTO
} from "@/types/user-action-operation"
import {
    collectionApi,
    upvoteApi,
    saveOperationApi,
    queryApi
} from "@/api/user-action-operation-api"
import {
    queryStrategyByScenicSpotIdApi
} from "@/api/scenic-spot-strategy-api"
import type { Category } from "@/types/category"
import { queryCategoryListByScenicSpotIdApi } from "@/api/category-api"

const router = useRouter()
const tabs = [{ value: 1, name: '介绍' }, { value: 2, name: '评论' }, { value: 3, name: '路线' }, { value: 4, name: '攻略' }, { value: 5, name: '门票' }]
// 响应式数据
const scenicSpotId = ref<number | null>(null)
const scenicSpotDetail = reactive<Partial<ScenicSpotVO>>({})
const isLoading = ref(false)
const showPreview = ref(false)
const currentPreviewImage = ref('')
const currentPreviewIndex = ref(0)
const actionOperationType = ref({
    'collectionType': 1,
    'upvoteType': 2,
    'viewType': 3,
    'stayType': 4
})
const collectionStatus = ref(false)
const upvoteStatus = ref(false)

const stayStartTime = ref(0)

const contentModule = ref("SCENIC_SPOT")

const tabIndex = ref(1) // 初始Tab为景点介绍

const scenicSpotStrategyListVO = ref<ScenicSpotStrategyListVO[]>([])

const userInfo = ref<Partial<User>>({})

const categoryList = ref<Category[]>([])

/**
 * 从localStorage里面。获取当前登录者的用户信息
 */
const loadUserInfo = async () => {
    const user = getUserInfo()
    if (!user) {
        ElMessage.error('用户信息认证异常')
        return
    }
    userInfo.value = user
}

// 景点攻略搜索
const goScenicSpotStrategySearch = () => {
    window.open(`/home-scenic-spot-strategy-list`);
}
// 跳转详情页
const viewStrategy = (id: number) => {
    window.open(`/scenic-spot-strategy-detail?scenicSpotStrategyId=${id}`);
}

// 计算属性
const previewImages = computed(() => {
    if (!scenicSpotDetail.brochureList) return []
    return scenicSpotDetail.brochureList
        .sort((a, b) => a.orderNumber - b.orderNumber)
        .map(brochure => brochure.coverUrl)
})

const handleTabChange = (tab: any) => {
    tabIndex.value = tab.value
}


// 获取景点ID
const getScenicSpotIdWithPath = () => {

    const queryId = router.currentRoute.value.query.scenicSpotId
    if (queryId) {
        scenicSpotId.value = Number(queryId)
    }

}

// 点赞操作
const upvoteOperation = async () => {
    try {
        if (!scenicSpotId.value) {
            return
        }
        const userActionOperation: UserActionOperation = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value
        }
        await upvoteApi(userActionOperation)

        ElMessage.success(upvoteStatus.value ? '取消点赞成功' : '点赞成功');

        // ElNotification.success({
        //     title: '用户操作',
        //     message: upvoteStatus.value ? '取消点赞成功' : '点赞成功',
        //     duration: 1500,
        //     position: 'top-right'
        // })
        upvoteStatus.value = !upvoteStatus.value

        await getScenicSpotDetail(scenicSpotId.value)
    } catch (error: any) {
        console.error("点赞或取消点赞操作异常:", error)
        ElNotification.warning({
            title: '用户操作',
            message: error,
            duration: 1500,
            position: 'top-right'
        })
    }
}

// 添加页面可见性监听
const handleVisibilityChange = () => {
    if (document.hidden) {
        recordStayOperation()
    }
}

// 记录用户的停留行为
const recordStayOperation = async () => {
    try {
        if (!scenicSpotId.value || stayStartTime.value === 0) {
            console.log('无法记录停留时长：缺少参数')
            return
        }

        // 计算停留时长（毫秒）
        const stayDuration = Date.now() - stayStartTime.value

        const userActionOperation: UserActionOperation = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value,
            type: actionOperationType.value.stayType,
            stayTime: stayDuration
        }

        await saveOperationApi(userActionOperation)
        console.log('停留时长记录成功')

    } catch (error: any) {
        console.error("记录用户停留行为操作异常:", error)
    }
}

// 记录用户的浏览行为
const recordViewOperation = async () => {
    try {
        if (!scenicSpotId.value) {
            return
        }
        const userActionOperation: UserActionOperation = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value,
            type: actionOperationType.value.viewType
        }
        await saveOperationApi(userActionOperation)
    } catch (error: any) {
        console.error("记录用户浏览行为操作异常:", error)
    }
}

// 收藏操作
const collectionOperation = async () => {
    try {
        if (!scenicSpotId.value) {
            return
        }
        const userActionOperation: UserActionOperation = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value
        }
        await collectionApi(userActionOperation)

        ElNotification.success({
            title: '用户操作',
            message: collectionStatus.value ? '取消收藏成功' : '收藏成功',
            duration: 1500,
            position: 'top-right'
        })
        collectionStatus.value = !collectionStatus.value

        await getScenicSpotDetail(scenicSpotId.value)
    } catch (error: any) {
        console.error("收藏或取消收藏操作异常:", error)
        ElNotification.warning({
            title: '用户操作',
            message: error,
            duration: 1500,
            position: 'top-right'
        })
    }
}

// 查询用户对于景点的收藏状态
const fetchCollectionStatus = async () => {
    try {
        if (!scenicSpotId.value) {
            return
        }
        const queryDTO: UserActionOperationQueryDTO = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value,
            type: actionOperationType.value.collectionType
        }
        const { data } = await queryApi(queryDTO)
        collectionStatus.value = data.length > 0
    } catch (error: any) {
        console.error("查询用户对于景点的收藏状态异常:", error)
    }
}

// 查询用户对于景点点赞状态
const fetchUpvoteStatus = async () => {
    try {
        if (!scenicSpotId.value) {
            return
        }
        const queryDTO: UserActionOperationQueryDTO = {
            contentModule: contentModule.value,
            contentId: scenicSpotId.value,
            type: actionOperationType.value.upvoteType
        }
        const { data } = await queryApi(queryDTO)
        upvoteStatus.value = data.length > 0
    } catch (error: any) {
        console.error("查询用户对于景点的点赞状态异常:", error)
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

// 获取景点详情
const getScenicSpotStrategyList = async (id: number | null) => {
    if (!id) {
        console.error("景点ID为空")
        return
    }
    try {
        const { data } = await queryStrategyByScenicSpotIdApi(id)
        scenicSpotStrategyListVO.value = data
    } catch (error: any) {
        console.error("查询景点关联的景点攻略信息异常：", error)
    }
}

// 获取景点关联的类别数据
const getCateoryList = async (id: number | null) => {
    if (!id) {
        console.error("景点ID为空")
        return
    }
    try {
        const { data } = await queryCategoryListByScenicSpotIdApi(id)
        categoryList.value = data
    } catch (error: any) {
        console.error("查询景点关联的类别数据异常：", error)
    }
}

// 图片预览功能
const previewImage = (imageUrl: string, index: number) => {
    if (!scenicSpotDetail.brochureList) return

    const sortedBrochures = scenicSpotDetail.brochureList
        .sort((a, b) => a.orderNumber - b.orderNumber)

    currentPreviewIndex.value = sortedBrochures.findIndex(b => b.coverUrl === imageUrl)
    if (currentPreviewIndex.value === -1) {
        currentPreviewIndex.value = index
    }

    currentPreviewImage.value = imageUrl
    showPreview.value = true
}

const closePreview = () => {
    showPreview.value = false
    currentPreviewImage.value = ''
    currentPreviewIndex.value = 0
}

const nextImage = () => {
    if (currentPreviewIndex.value < previewImages.value.length - 1) {
        currentPreviewIndex.value++
        currentPreviewImage.value = previewImages.value[currentPreviewIndex.value] || ''
    }
}

const prevImage = () => {
    if (currentPreviewIndex.value > 0) {
        currentPreviewIndex.value--
        currentPreviewImage.value = previewImages.value[currentPreviewIndex.value] || ''
    }
}


onBeforeRouteLeave(async (to, from, next) => {
    console.log('路由离开，记录停留时长')
    await recordStayOperation()
    next() // 继续导航
})


onUnmounted(() => {
    // 移除监听
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    console.log('组件卸载，记录停留时长')
    recordStayOperation().catch(error => {
        console.error('记录停留时长失败:', error)
    })
})

onMounted(async () => {
    stayStartTime.value = Date.now()
    // 添加页面可见性监听
    document.addEventListener('visibilitychange', handleVisibilityChange)
    getScenicSpotIdWithPath()
    if (scenicSpotId.value) {
        await getScenicSpotDetail(scenicSpotId.value)
        await fetchCollectionStatus()
        await fetchUpvoteStatus()
        await recordViewOperation()
        await getScenicSpotStrategyList(scenicSpotId.value)
        await loadUserInfo()
        await getCateoryList(scenicSpotId.value)
    }
})
</script>

<style lang="scss" scoped>
.tab-item {
    padding: 6px 30px;
    cursor: pointer;
    border-radius: 30px;
    font-size: 18px;
}

.content {

    display: flex;
    justify-content: center;
    align-content: center;

    .container {
        max-width: 800px;

        .brochure-container {
            padding: 4px;
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
        }

        .title {
            font-size: 42px;
        }

        .summary {
            margin-block: 20px;
            padding: 20px;
            border-radius: 10px;
            background-color: rgb(248, 249, 250);
        }
    }
}


/* 基础变量 */
:root {
    --primary-color: #4f46e5;
    --primary-hover: #4338ca;
    --secondary-color: #6b7280;
    --success-color: #10b981;
    --danger-color: #ef4444;
    --warning-color: #f59e0b;
    --border-color: #e5e7eb;
    --bg-color: #f1f1f1;
    --card-bg: #ffffff;
    --text-primary: #111827;
    --text-secondary: #6b7280;
    --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
    --shadow-md: 0 4px 6px -1px rgb(0 0 0 / 0.1);
    --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.2);
    --radius-sm: 0.375rem;
    --radius-md: 0.5rem;
    --radius-lg: 0.75rem;
}

.title {
    font-weight: 600;
    font-size: 30px;
}

.location {
    display: flex;
    margin-block: 12px;
    justify-content: left;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    color: #6b7280;
}

.action-container {
    position: fixed;
    top: 230px;
    left: 200px;
    /* width: 50px; */
    /* height: 100px; */
    padding: 20px 10px;
    // box-shadow: 0 6px 12px rgb(240, 240, 240);
    border-radius: 10px;
    z-index: 1000;
    background-color: rgb(255, 255, 255);

    .item {

        padding: 12px;
        border-radius: 10px;
        cursor: pointer;

        .children {
            text-align: center;
            font-size: 12px;
            font-weight: 600;
            min-width: 50px;

        }


        &:hover {
            background-color: rgb(244, 244, 244);
            color: #333;
        }
    }
}


.header {

    width: 100%;
    padding: 12px 140px;
    position: sticky;
    top: 0;
    background-color: rgb(255, 255, 255);
    z-index: 100;

    .logo {
        display: flex;
        justify-content: left;
        align-items: center;
        gap: 10px;
        min-width: 200px;
        flex: 1;

        img {
            width: 30px;
            height: 30px;
        }

        .system-name {
            font-size: 32px;
            color: #333;
            font-weight: 500;
        }
    }
}

/* 容器样式 */
.scenic-detail-container {
    min-height: 100vh;
    background-color: var(--bg-color);
    padding-bottom: 2rem;
}

/* 返回按钮 */
.back-header {
    font-size: 20px;
}

.back-btn {
    background: transparent;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    padding: 0.5rem 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    color: var(--text-primary);
    font-size: 0.875rem;
    transition: all 0.2s;
}

.back-btn:hover {
    background: var(--bg-color);
    border-color: var(--primary-color);
    color: var(--primary-color);
}

.back-icon {
    font-size: 1.25rem;
}


/* 封面区域 */
.cover-section {
    position: relative;
    height: 400px;
    border-radius: var(--radius-lg);
    overflow: hidden;
    border-radius: 10px;
    margin: 1rem 0 2rem;
    box-shadow: var(--shadow-lg);
}

.cover-image {
    width: 100%;
    height: 100%;
    border-radius: 20px;
    object-fit: cover;
    padding: 10px;
}

.cover-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
    padding: 2rem;
    color: white;
}

.spot-title {
    font-size: 2rem;
    font-weight: 700;
    margin: 0 0 0.5rem 0;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.location-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1rem;
    opacity: 0.9;
}

.location-icon {
    font-size: 1.25rem;
}

/* 信息卡片 */
.info-cards {
    max-width: 800px;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.info-card:first-child {
    background-color: rgb(250, 250, 250);
    max-height: 350px;
}

.info-card {
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    padding: 1.5rem;
    /* background-color: rgb(248, 248, 248); */
    border-radius: 20px;
    cursor: pointer;
    box-shadow: var(--shadow-md);
    transition: transform 0.3s, box-shadow 0.3s;

    /* &:hover {
        background-color: rgb(246, 246, 246);
    } */
}

.info-card:hover {
    /* transform: translateY(-2px); */
    box-shadow: var(--shadow-lg);
}

.card-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.25rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 1rem 0;
    padding-bottom: 0.75rem;
    border-bottom: 2px solid var(--border-color);
}

.title-icon {
    font-size: 1.5rem;
}

.summary-text {
    line-height: 1.6;
    color: var(--text-secondary);
    margin-bottom: 1rem;
    font-size: 0.95rem;
}

.create-time {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.875rem;
    color: var(--text-secondary);
    margin-top: 1rem;
    padding-top: 1rem;
    border-top: 1px solid var(--border-color);
}

.time-icon {
    font-size: 1rem;
}

.region-info,
.id-info {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.region-item,
.id-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 0;
    border-bottom: 1px solid var(--border-color);
}

.region-item:last-child,
.id-item:last-child {
    border-bottom: none;
}

.region-label,
.id-label {
    font-weight: 500;
    color: var(--text-primary);
}

.region-value,
.id-value {
    color: var(--text-secondary);
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

/* 内容区域 */
.content-section {
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 2rem;
    box-shadow: var(--shadow-md);
}

.section-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 1.5rem 0;
}

.content-wrapper {
    width: 800px;

    color: var(--text-secondary);
}

.content-wrapper :deep(p) {
    margin: 1rem 0;
}

.content-wrapper :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: var(--radius-md);
    margin: 1rem 0;
}

/* 图册区域 */
.brochure-section {
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    /* padding: 2rem; */
    margin-bottom: 2rem;
    box-shadow: var(--shadow-md);
}

.brochure-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 1.5rem;
    /* margin-top: 1.5rem; */
}

.brochure-item {
    border-radius: var(--radius-md);
    overflow: hidden;
    border: 1px solid var(--border-color);
    transition: all 0.3s;
    cursor: pointer;
}

.brochure-item:hover {
    border-color: var(--primary-color);
    /* transform: translateY(-2px); */
    box-shadow: var(--shadow-lg);
}

.brochure-image-wrapper {
    position: relative;
    border-radius: 10px;
    height: 200px;
    overflow: hidden;
}

.brochure-image {
    width: 100%;
    height: 100%;
    border-radius: 10px;
    object-fit: cover;
    transition: transform 0.4s;
}

.image-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    font-size: 12px;
    transition: opacity 0.3s;
}

.brochure-item:hover .image-overlay {
    opacity: 1;
}

.preview-text {
    color: white;
    font-weight: 500;
    font-size: 12px;
    padding: 2px 6px;
    background: rgb(35, 167, 242);
    border-radius: 20px;
}

.image-order {
    position: absolute;
    top: 0.5rem;
    left: 0.5rem;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    padding: 2px 6px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
}

.brochure-info {
    padding: 1rem;
    background: var(--bg-color);
    border-top: 1px solid var(--border-color);
}

.info-text {
    font-size: 0.875rem;
    color: var(--text-secondary);
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

/* 空状态 */
.empty-brochure {
    text-align: center;
    padding: 4rem 1rem;
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    color: var(--text-secondary);
    box-shadow: var(--shadow-md);
}

.empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
}

.empty-text {
    font-size: 1.125rem;
}

/* 加载状态 */
.loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 6rem 1rem;
}

.loading-spinner {
    width: 50px;
    height: 50px;
    border: 3px solid var(--border-color);
    border-top-color: var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1rem;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

.loading-text {
    color: var(--text-secondary);
    font-size: 1.125rem;
}

/* 错误状态 */
.error-container {
    text-align: center;
    padding: 6rem 1rem;
}

.error-icon {
    font-size: 4rem;
    color: var(--danger-color);
    margin-bottom: 1rem;
}

.error-text {
    font-size: 1.25rem;
    color: var(--text-primary);
    margin-bottom: 2rem;
}

.retry-btn {
    background: var(--primary-color);
    color: white;
    border: none;
    border-radius: var(--radius-md);
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    cursor: pointer;
    transition: background 0.2s;
}

.retry-btn:hover {
    background: var(--primary-hover);
}

/* 预览模态框 */
.preview-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.9);
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

.modal-content {
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
}

.modal-close {
    position: absolute;
    top: -50px;
    right: 0;
    background: none;
    border: none;
    color: white;
    font-size: 2.5rem;
    cursor: pointer;
    padding: 0.5rem;
    opacity: 0.7;
    transition: opacity 0.2s;
}

.modal-close:hover {
    opacity: 1;
}

.preview-img {
    max-width: 100%;
    max-height: 70vh;
    object-fit: contain;
    border-radius: var(--radius-md);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-controls {
    position: absolute;
    bottom: -60px;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2rem;
    color: white;
}

.nav-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    width: 44px;
    height: 44px;
    border-radius: 50%;
    font-size: 1.5rem;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.nav-btn:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.1);
}

.nav-btn:disabled {
    opacity: 0.3;
    cursor: not-allowed;
}

.image-counter {
    font-size: 1rem;
    opacity: 0.8;
    min-width: 80px;
    text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .cover-section {
        height: 300px;
    }

    .spot-title {
        font-size: 1.5rem;
    }

    .info-cards {
        grid-template-columns: 1fr;
    }

    .brochure-grid {
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 1rem;
    }
}

@media (max-width: 480px) {
    .cover-section {
        height: 200px;
    }

    .spot-title {
        font-size: 1.25rem;
    }

    .brochure-grid {
        grid-template-columns: 1fr;
    }

    .modal-controls {
        bottom: -80px;
    }
}
</style>