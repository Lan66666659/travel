<template>
    <div class="timeline-arc-container">
        <div style="display: flex;justify-content: center;align-items: center;margin-block: 10px;">
            <div style="display: flex;align-items: center;gap: 20px;">
                <div style="width: 100px;height: 2px;background-color: rgb(200,200,200);"></div>
                <div style="text-align: center;">
                    <div style="font-size: 40px;">{{ title }}</div>
                    <div style="color: rgb(5, 112, 147);">{{ subTitle }}</div>
                </div>
                <div style="width: 100px;height: 2px;background-color: rgb(200,200,200);"></div>
            </div>
        </div>
        <div class="strategy-container">
            <!-- 景点攻略进行渲染 -->
            <div class="item-strategy" :key="`strategy-${strategy.id}-${strategy.title}`"
                v-for="strategy in scenicSpotStrategyList">
                <div class="cover">
                    <img :src="strategy.cover" alt="" srcset="">
                </div>
                <div class="title" @click="view(strategy.id)">{{ strategy.title }}</div>
                <div class="user-info">
                    <img :src="strategy.avatar" alt="" srcset="">
                    <span class="username">{{ strategy.username }}</span>
                </div>
            </div>
        </div>
        <div style="margin-block: 20px;display: flex;justify-content: center;align-items: center;">
            <div class="fun-btn" @click.stop="handleScenicSpotStrategySearch">去查找景点攻略</div>
        </div>

    </div>
</template>

<script setup lang="ts">

// 父组件接收的数据
const props = defineProps<{
    scenicSpotStrategyList: Array<{
        id?: number
        title?: string
        cover?: string
        summary?: string
        createTime?: Date
        avatar?: string
        username?: string
        auditStatus?: boolean
    }>;
    title: string;
    subTitle: string;
}>()

// 定义事件
const emit = defineEmits<{
    (e: 'view', id: number): void,
    (e: 'search', id: number | null): void,
}>()

// 查看景点攻略详情
const view = (id: number | undefined) => {
    if (!id) return
    emit('view', id)
}

// 景点攻略查询页
const handleScenicSpotStrategySearch = () => {
    emit('search', null)
}


</script>

<style lang="scss" scoped>
.fun-btn {
    padding: 6px 20px;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.5s ease;
    background-color: rgb(51, 51, 51);
    color: rgb(255, 255, 255);

    &:hover {
        background-color: rgb(31, 31, 31);
    }
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
            color: rgb(65, 132, 170);

            &:hover {
                color: rgb(49, 111, 147);
            }
        }

        .cover {
            img {
                width: 100%;
                height: 140px;
                border-radius: 10px;
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
            background-color: rgba(0,0,0,0.6);
            padding: 4px 10px;
            border-radius: 20px;

            img {
                width: 20px;
                height: 20px;
                border-radius: 50%;
            }

            .username {
                width: 50px;
                text-align: center;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
                font-size: 12px;
                color: rgb(255, 255, 255);
            }
        }
    }
}
</style>