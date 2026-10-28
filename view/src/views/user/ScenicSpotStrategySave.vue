<template>
    <div class="scenic-spot-save-container">
        <!-- 主容器 -->
        <div class="main-container">
            <div class="page-header">
                <h2 class="header-title">发布攻略</h2>
            </div>
            <!-- 步骤条 -->
            <div class="steps-wrapper">
                <el-steps :active="activeStepIndex" finish-status="success" class="steps-bar" space="auto">
                    <el-step v-for="(step, index) in steps" :key="index" :title="step.title" :icon="step.icon"
                        class="step-item"></el-step>
                </el-steps>
            </div>
            <!-- 操作按钮区 -->
            <div class="action-bar">
                <el-button round type="primary" @click="last" size="mini" class="action-btn prev-btn"
                    :disabled="activeStepIndex === 1">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                    上一步
                </el-button>
                <div class="action-right">
                    <el-button type="primary" round @click="next" size="mini" class="action-btn next-btn"
                        :disabled="activeStepIndex === 3">
                        下一步
                        <el-icon>
                            <ArrowRight />
                        </el-icon>
                    </el-button>
                    <el-button round type="primary" size="mini" class="action-btn submit-btn"
                        @click="saveScenicSpotStraegy" v-if="activeStepIndex === 3">
                        <el-icon style="margin-right: 10px;">
                            <CircleCheckFilled />
                        </el-icon>
                        新增景点攻略
                    </el-button>
                </div>
            </div>

            <!-- 内容卡片 -->
            <div class="content-card">
                <!-- 基本信息区域 -->
                <div class="form-section" v-if="activeStepIndex === 1">
                    <div class="form-group">
                        <label class="form-label">标题 <span class="required">*</span></label>
                        <el-input v-model="scenicSpotStraegy.title" placeholder="请输入景点攻略标题（100字以内）" maxlength="100"
                            show-word-limit size="large" class="form-input"></el-input>
                    </div>

                    <div class="form-group" style="margin-bottom: 20px;">
                        <label class="form-label">封面 <span class="required">*</span></label>
                        <div class="upload-cover-wrapper">
                            <div class="cover-preview" v-if="cover">
                                <img :src="cover" alt="封面预览" class="cover-img" />
                                <div @click="cover = ''" class="cover-remove-btn">
                                    <span>
                                        <el-icon>
                                            <delete />
                                        </el-icon>

                                    </span>
                                    <span>
                                        移除
                                    </span>
                                </div>

                            </div>
                            <el-upload class="cover-uploader" :action="uploadApi" :show-file-list="false"
                                :on-success="handleImageSuccess" :before-upload="beforeCoverUpload">
                                <el-button type="primary" round size="mini" class="upload-btn">
                                    <el-icon>
                                        <UploadFilled />
                                    </el-icon>
                                    {{ cover ? '更换封面' : '上传封面' }}
                                </el-button>
                                <div class="upload-tip">支持JPG/PNG格式，大小不超过2MB</div>
                            </el-upload>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">摘要 <span class="required">*</span></label>
                        <el-input type="textarea" :rows="4" placeholder="请输入景点攻略摘要（200字以内）"
                            v-model="scenicSpotStraegy.summary" maxlength="200" show-word-limit size="large"
                            class="form-textarea"></el-input>
                    </div>
                </div>
                <!-- 景点攻略介绍区域 -->
                <div class="form-section" v-if="activeStepIndex === 2">
                    <label class="form-label">内容 <span class="required">*</span></label>
                    <WangEditor :receive-content="contentWithImage" height="500px" :api="uploadApi"
                        @on-listener="handleContentWithImageChange" class="editor-wrapper" />
                </div>
                <!-- 关联的景点信息 -->
                <div v-if="activeStepIndex === 3">
                    <div>
                        <div class="scenic-spot-link" v-if="!isCreateModel">当前关联：景点ID【{{ scenicSpot.scenicSpotId }}】 -
                            {{ scenicSpot.scenicSpotTitle }}</div>
                        <el-autocomplete style="width: 95%;" v-model="searchKey" :fetch-suggestions="querySearchAsync"
                            placeholder="请输入内容" @select="handleSelect"></el-autocomplete>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive } from "vue"
import { ElMessage, ElNotification } from "element-plus"
import WangEditor from '@/components/Editor.vue'
import type {
    ScenicSpotStrategy,
} from "@/types/scenic-spot-strategy.ts"
import {
    Delete,
} from '@element-plus/icons-vue'
import { addScenciSpotStrategyApi } from "@/api/scenic-spot-strategy-api.ts"
import { queryScenicSpotApi } from "@/api/scenic-spot-api"
import router from "@/router"

const isCreateModel = ref(true)
const searchKey = ref('')
const scenicSpot = ref({
    scenicSpotId: null,
    scenicSpotTitle: ''
})
const activeStepIndex = ref(1) // 默认步骤1
const scenicSpotStraegy = reactive<Partial<ScenicSpotStrategy>>({})
const cover = ref('')
const steps = [
    { title: "步骤一：基本信息", icon: "Tickets" },
    { title: "步骤二：景点攻略介绍", icon: "Edit" },
    { title: "步骤三：关联景点", icon: "MessageBox" }
];

const uploadApi = ref('http://localhost:21090/api/v1.0/online-travel-api/file/upload')
const contentWithImage = ref('<p>介绍景点攻略吧</p>')

const handleSelect = (item: any) => {
    scenicSpot.value.scenicSpotId = item.id
    scenicSpot.value.scenicSpotTitle = item.value
}

const querySearchAsync = async (queryString: string, cb: any) => {
    try {
        const { data } = await queryScenicSpotApi({
            title: queryString,
            current: 1,
            size: 100
        } as any)
        const suggestions = (data || []).map((item: any) => ({
            value: item.title,  // 显示的值
            id: item.id,        // 保留原始ID
            ...item             // 保留其他属性
        }))
        cb(suggestions)
    } catch (error) {
        console.log("查询景点数据异常");
    }
}


const next = () => {
    if (activeStepIndex.value++ > 3) {
        activeStepIndex.value = 1
    }
}
const last = () => {
    if (activeStepIndex.value-- === 1) {
        activeStepIndex.value = 3
    }
}

const beforeCoverUpload = (file: File) => {
    const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
    const isLt2M = file.size / 1024 / 1024 < 2

    if (!isJPGOrPNG) {
        ElMessage.error('封面只能是 JPG/PNG 格式!')
        return false
    }
    if (!isLt2M) {
        ElMessage.error('封面大小不能超过 2MB!')
        return false
    }
    return true
}

const handleImageSuccess = (response: any) => {
    if (response.code === 200) {
        cover.value = response.data
        ElMessage.success('封面上传成功')
    } else {
        ElMessage.error(response.message || '封面上传失败')
    }
}

const handleContentWithImageChange = (newContent: string) => {
    contentWithImage.value = newContent
}

// 景点攻略新增
const saveScenicSpotStraegy = async () => {
    // 参数校验，封装数据，发请求，根据请求结果做后续动作
    if (!scenicSpotStraegy.title) {
        ElNotification.warning({
            title: '参数校验',
            message: '标题不为空',
            duration: 1500,
            position: 'bottom-right'
        })
        return;
    }
    if (!cover.value) {
        ElNotification.warning({
            title: '封面检验',
            message: '请上传封面',
            duration: 1500,
            position: 'bottom-right'
        })
        return;
    }
    if (!scenicSpotStraegy.summary) {
        ElNotification.warning({
            title: '参数校验',
            message: '请补充摘要',
            duration: 1500,
            position: 'bottom-right'
        })
        return;
    }
    if (!contentWithImage.value) {
        ElNotification.warning({
            title: '参数校验',
            message: '请补充景点攻略介绍',
            duration: 1500,
            position: 'bottom-right'
        })
        return;
    }
    // 封面
    scenicSpotStraegy.cover = cover.value
    // 内容
    scenicSpotStraegy.content = contentWithImage.value
    // 设置景点ID
    if (!scenicSpot.value.scenicSpotId) {
        ElNotification.warning({
            title: '参数校验',
            message: '请选择关联景点',
            duration: 1500,
            position: 'bottom-right'
        })
        return;
    }
    scenicSpotStraegy.scenicSpotId = scenicSpot.value.scenicSpotId
    try {
        await addScenciSpotStrategyApi(scenicSpotStraegy as any)
        ElMessage.success('景点攻略新增成功')
        router.push('/user')  // 返回首页
    } catch (error: any) {
        console.log('景点攻略新增异常：', error);
        ElMessage.error(`景点攻略新增异常： ${error}`)
    }

}

</script>
<style lang="scss" scoped>
// 全局变量
$primary-color: #409eff;
$success-color: #67c23a;
$warning-color: #e6a23c;
$danger-color: #f56c6c;
$text-primary: #303133;
$text-regular: #606266;
$text-secondary: #909399;
$border-color: #e4e7ed;
$bg-color: #fff;
$transition-base: all 0.3s ease;

// 主容器
.scenic-spot-save-container {
    min-height: 100vh;
    background-color: $bg-color;
    padding: 0 20px;
}

// 页头
.page-header {
    padding: 10px 0;
    margin-bottom: 30px;
    margin-top: 30px;

    .header-content {
        --el-page-header-text-color: $text-primary;

        .header-title {
            font-size: 20px;
            font-weight: 600;
            margin: 0;
        }
    }
}

// 主内容容器
.main-container {
    max-width: 1200px;
    margin: 0 auto;
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
}

// 步骤条
.steps-wrapper {
    padding: 20px;
    background-color: rgb(250, 250, 250);
    border: 1px solid rgb(240, 240, 240);
    display: flex;
    justify-content: left;
    border-radius: 14px;
    gap: 30px;

    .steps-bar {
        --el-steps-finish-color: $primary-color;
        --el-steps-process-color: $primary-color;

        .step-item {
            margin-right: 30px;

            .el-step__title {
                font-size: 14px;
                margin-top: 8px;
            }

            .el-step__description {
                font-size: 12px;
            }
        }
    }
}

// 步骤定义（脚本未改，模板中循环展示）
$steps: (
    (title: "基本信息", icon: "UserFilled"),
    (title: "地区信息", icon: "LocationFilled"),
    (title: "类别信息", icon: "Menu"),
    (title: "景点攻略介绍", icon: "Edit"),
    (title: "景点攻略图册", icon: "PictureFilled")
);

// 内容卡片
.content-card {
    padding: 10px 0;
}

// 表单通用样式
.form-section {


    .form-label {
        display: block;
        font-size: 20px;
        color: #8d8e90;
        color: $text-primary;
        margin-bottom: 8px;

        .required {
            color: $danger-color;
            margin-left: 4px;
        }
    }

    .form-input,
    .form-textarea {
        --el-input-border-radius: 8px;
        --el-input-hover-border-color: $primary-color;
        // --el-input-focus-border-color: $primary-color;
        width: 100%;
        margin-bottom: 20px;
    }

    .empty-tip {
        padding: 40px 0;
        text-align: center;
    }
}

// 封面上传样式
.upload-cover-wrapper {
    display: flex;
    align-items: center;
    gap: 20px;

    .cover-preview {
        position: relative;
        width: 180px;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;
        border: 1px dashed $border-color;

        .cover-img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .cover-remove-btn {
            position: absolute;
            top: 8px;
            right: 8px;
            width: 68px;
            height: 28px;
            background-color: rgba(0, 0, 0, 0.5);
            border-radius: 20px;
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 6px;
            cursor: pointer;

            &:hover {
                background-color: rgba(0, 0, 0, 0.8);
            }
        }
    }

    .cover-uploader {
        flex: 1;

        .upload-btn {
            --el-button-border-radius: 8px;
            padding: 12px 24px;
        }

        .upload-tip {
            margin-top: 8px;
            font-size: 12px;
            margin-left: 20px;
            color: $text-secondary;
        }
    }
}

// 地区/类别选择样式
.region-group,
.category-group {

    .region-level,
    .category-level {
        margin-bottom: 24px;

        .radio-group {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;

            .radio-item {
                --el-radio-font-size: 14px;
                --el-radio-checked-color: $primary-color;
                padding: 8px 16px;
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

// 编辑器样式
.editor-wrapper {
    --el-editor-border-radius: 8px;
    border: 1px solid $border-color;
    border-radius: 8px;
    overflow: hidden;
}

// 图册区域样式
.album-section {
    .album-upload-wrapper {
        margin-bottom: 24px;

        .album-uploader {
            --el-upload-picture-card-width: 120px;
            --el-upload-picture-card-height: 120px;
            --el-upload-item-border-radius: 8px;

            .upload-card {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                // height: 100%;
                padding: 20px;
                border: 1px dashed $border-color;
                border-radius: 8px;
                cursor: pointer;
                transition: $transition-base;

                &:hover {
                    border-color: $primary-color;
                    background-color: rgba(64, 158, 255, 0.05);
                }

                .upload-text {
                    margin-top: 8px;
                    font-size: 12px;
                    color: $text-secondary;
                }
            }

            :deep(.el-upload-list__item) {
                overflow: hidden;
                border-radius: 8px;
            }
        }

        .upload-tip {
            margin-top: 8px;
            font-size: 12px;
            color: $text-secondary;
        }
    }

    .album-sort-wrapper {
        .sort-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 16px;
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
        }

        .sort-grid {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;
        }

        .sort-item {
            width: 120px;
            height: 120px;
            cursor: grab;
            transition: $transition-base;

            &.dragging {
                opacity: 0.7;
                transform: scale(1.05);
                box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
            }

            &.drag-over {
                border: 2px dashed $primary-color;
            }

            .image-card {
                position: relative;
                width: 100%;
                height: 100%;
                border-radius: 8px;
                overflow: hidden;
                border: 1px solid $border-color;

                .sort-img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }

                .image-index {
                    position: absolute;
                    top: 4px;
                    left: 4px;
                    width: 20px;
                    height: 20px;
                    background: rgba(0, 0, 0, 0.5);
                    color: #fff;
                    font-size: 12px;
                    border-radius: 4px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }

                .image-actions {
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    display: flex;
                    justify-content: center;
                    gap: 8px;
                    padding: 4px;
                    background: linear-gradient(transparent, rgba(0, 0, 0, 0.6));
                    opacity: 0;
                    transition: $transition-base;

                    .action-btn {
                        width: 28px;
                        height: 28px;
                        padding: 0;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        color: #fff;

                        &.preview-btn {
                            --el-icon-color: #fff;
                        }

                        &.delete-btn {
                            --el-icon-color: #fff;
                        }
                    }
                }

                &:hover .image-actions {
                    opacity: 1;
                }
            }
        }
    }
}

// 操作按钮栏
.action-bar {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 10px;
    padding: 20px 0;
    margin-top: 10px;

    .action-btn {
        --el-button-border-radius: 8px;
        padding: 12px 24px;
        font-size: 14px;
        font-weight: 500;
    }


}

// 图片预览器
.image-viewer {
    z-index: 9999;
}

:deep(.el-step__icon) {
    background: rgb(250, 250, 250);
}

// 响应式适配
@media (max-width: 768px) {
    .scenic-spot-save-container {
        padding: 0 10px;
    }

    .main-container {
        border-radius: 8px;
    }

    .steps-wrapper,
    .content-card,
    .action-bar {
        padding: 16px;
    }

    .upload-cover-wrapper {
        flex-direction: column;
        gap: 12px;

        .cover-preview {
            width: 100%;
            max-width: 200px;
        }
    }

    .region-group,
    .category-group {
        .radio-group {
            flex-direction: column;
            gap: 12px;

            .radio-item {
                width: 100%;
            }
        }
    }

    .album-section {
        .sort-grid {
            justify-content: center;
        }
    }

    .action-bar {
        flex-direction: column;
        gap: 12px;

        .action-right {
            width: 100%;
            justify-content: flex-end;
        }

        .action-btn {
            width: 100%;
            margin-left: 0 !important;
        }
    }
}
</style>