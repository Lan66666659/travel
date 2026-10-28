<template>

    <div style="padding-inline: 160px;">
        <!-- 收藏的景点信息 -->
        <div>
            <CollectionScenicSpotList />
        </div>
        <!-- 收藏的景点攻略信息 -->
         <div>
            <CollectionScenicSpotStrategy/>
         </div>
    </div>

</template>
<script setup lang="ts">
import { ref } from "vue"
import CollectionScenicSpotList from "./CollectionScenicSpotList.vue"
import CollectionScenicSpotStrategy from "./CollectionScenicSpotStrategy.vue"
</script>
<style lang="scss" scoped></style>