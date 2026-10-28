<template>
  <div class="user-layout">
    <div class="bottom-banner">
      <div class="carousel-container">
        <img :src="carouselList[currentCarouselIndex]?.coverUrl" alt="" srcset="">
        <div class="basic-info">
          <div class="title">{{ carouselList[currentCarouselIndex]?.title }}</div>
          <div class="detail">{{ carouselList[currentCarouselIndex]?.recommendDetail }}</div>
          <div class="go" @click="goDetail(carouselList[currentCarouselIndex]?.scenicSpotId)">查看详情</div>
        </div>
      </div>
      <div class="hover"></div>
      <div class="header">
        <!-- logo部分 -->
        <div class="logo">
          <img src="/logo.png" alt="">
          <div class="system-name">海牛旅游网</div>
        </div>
        <!-- 功能区 -->
        <ul class="nav-list">
          <li :style="{
            backgroundColor: defaultPathKey === 'home' ? 'rgb(29, 191, 79)' : '',
            fontWeight: defaultPathKey === 'home' ? 'bold' : ''
          }" @click="viewerRouteClick('home')">景点</li>
          <li :style="{
            backgroundColor: defaultPathKey === 'strategy' ? 'rgb(29, 191, 79)' : '',
            fontWeight: defaultPathKey === 'strategy' ? 'bold' : ''
          }" @click="viewerRouteClick('strategy')">景点攻略</li>
        </ul>
        <!-- 个人信息 -->
        <div class="info-container" @click="$router.push('/login')">
          登录/注册
        </div>
      </div>
    </div>
    <!-- 页面内容 -->
    <div class="content">
      <!-- 景点 -->
      <IndexScenicSpot v-if="defaultPathKey === 'home'" />
      <!-- 景点攻略 -->
      <IndexStrategy v-if="defaultPathKey === 'strategy'" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import IndexScenicSpot from './IndexScenicSpot.vue'
import IndexStrategy from './IndexStrategy.vue'
import { ElMessage } from 'element-plus'
import {
  getToken
} from '@/utils/storage'

import {
  queryViewerCarouselApi
} from "@/api/viewer-api"

import { tokenAuthApi } from "@/api/user-api"

import type {
  Carousel,
  CarouselQueryDto
} from "@/types/carousel"

const router = useRouter()

// 轮播响应式数据
const currentCarouselIndex = ref(0) // 当前轮播图索引
let carouselTimer: any = null // 定时器
const carouselInterval = 5000 // 5秒间隔
const defaultPathKey = ref('home')

// 跳转景点详情页 - 游客
const goDetail = (id: number | undefined) => {
  window.open(`/viewer-scenic-spot-info?scenicSpotId=${id}`)
}

const viewerRouteClick = (key: string) => {
  defaultPathKey.value = key
}

const carouselList = ref<Carousel[]>([])
const carouselQueryDTO = reactive<Partial<CarouselQueryDto>>({})

// 启动轮播图定时器
const startCarouselTimer = () => {
  // 先清除已有的定时器
  stopCarouselTimer()

  carouselTimer = setInterval(() => {
    if (carouselList.value.length > 0) {
      // 计算下一个索引
      currentCarouselIndex.value = (currentCarouselIndex.value + 1) % carouselList.value.length
    }
  }, carouselInterval)
}

// 停止轮播图定时器
const stopCarouselTimer = () => {
  if (carouselTimer) {
    clearInterval(carouselTimer)
    carouselTimer = null
  }
}

// 获取并校验token
const tokenAuth = async () => {
  const token = getToken()
  // token都没有，肯定没登录
  if (!token) {
    console.log("用户没登录...")
    return
  }
  // 有token，但是不一定是对的，所以需要先去后端校验
  try {
    const { data } = await tokenAuthApi()
    if (data.role === 1) {
      router.push('/admin')
    } else if (data.role === 2) {
      router.push('/user')
    }
  } catch (error) {
    console.error("token校验异常...")
  }

}

// 查询轮播图列表
const fetchCarouselData = async () => {
  try {
    const response = await queryViewerCarouselApi(carouselQueryDTO as any)
    carouselList.value = response.data
    if (carouselList.value.length > 0) {
      startCarouselTimer()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取轮播图数据失败')
  }
}

// 组件挂载时
onMounted(async () => {
  // 记载轮播图数据
  await fetchCarouselData()
  // token校验
  await tokenAuth()
})

// 确保清理事件监听器
onUnmounted(async () => {
  // 恢复文本选择（安全措施）
  document.body.style.userSelect = ''
  document.body.style.webkitUserSelect = ''
  document.body.style.cursor = ''
  await stopCarouselTimer()
})
</script>

<style scoped lang="scss">
.bottom-banner {
  position: relative;
  background-color: rgb(255, 255, 255);

  .hover {
    position: absolute;
    top: 0;
    width: 100%;
    height: calc(100% - 6px);
    background-color: rgba(0, 0, 0, 0.3);
    z-index: 10;
    transition: all 1.2s ease;
    cursor: pointer;

    &:hover {
      background-color: rgba(0, 0, 0, 0.4);
    }
  }

  .carousel-container {
    img {
      width: 100%;
      aspect-ratio: 3/1;
      object-fit: cover;
    }

    .basic-info {
      position: absolute;
      top: 150px;
      left: 50px;
      z-index: 20;

      .go {
        background-color: rgb(255, 255, 255);
        display: inline-block;
        padding: 12px 30px;
        border-radius: 30px;
        margin-top: 20px;
        cursor: pointer;
      }

      .title {
        font-size: 50px;
        font-family: Impact, Haettenschweiler, 'Arial Narrow Bold', sans-serif;
        font-weight: 600;
        color: rgb(255, 255, 255);
      }

      .detail {
        font-size: 20px;
        min-height: 100px;
        font-family: Impact, Haettenschweiler, 'Arial Narrow Bold', sans-serif;
        font-weight: 600;
        color: rgb(255, 255, 255);
        padding-right: 50px;
      }

    }
  }


}


.user-layout {
  width: 100%;
  min-height: 100vh;
}

.content {
  // padding-inline: 200px;
  padding-block: 30px;
  box-sizing: border-box;
}

.header {
  z-index: 20;
  display: flex;
  width: 100%;
  justify-content: space-evenly;
  align-items: center;
  height: 80px;
  padding-inline: 50px;
  // background-color: rgba(0, 0, 0, 0.3);
  position: absolute;
  top: 0;
  right: 0;

  .logo {
    display: flex;
    justify-content: left;
    align-items: center;
    gap: 10px;
    flex: 1;

    img {
      width: 30px;
      height: 30px;
    }

    .system-name {
      font-size: 26px;
      color: #fff;
      font-weight: 500;
    }
  }
}

.nav-list {
  list-style: none;
  padding-left: 0;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: left;
  gap: 40px;
  flex: 2;

  li {
    font-size: 18px;
    color: #fff;
    cursor: pointer;
    padding: 4px 20px;
    position: relative;
    border-radius: 20px;
    transition: color 0.3s;

    &.active {
      background-color: rgb(29, 191, 79);
      color: rgb(255, 255, 255);
      font-weight: bold;
    }
  }
}

.info-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;

  background-color: rgb(255, 255, 255);
  padding: 12px 32px;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.3s ease;

  font-weight: bold;

  &:hover {
    background-color: rgb(29, 191, 79);
    color: rgb(255, 255, 255);
  }

}

.avatar-upload-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.tip-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>