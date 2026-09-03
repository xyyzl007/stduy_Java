<template>
  <div class="common-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="logo">
        <el-icon :size="24"><Reading /></el-icon>
        <span style="margin-left: 10px; font-weight: bold; font-size: 18px;">学生实验报告提交与批阅系统</span>
      </div>
      <div class="user-info">
        <el-radio-group v-model="currentRole" size="small" @change="switchRole">
          <el-radio-button label="STUDENT">当前身份：李同学 (学生端)</el-radio-button>
          <el-radio-button label="TEACHER">切换为：张老师 (教师端)</el-radio-button>
        </el-radio-group>
      </div>
    </el-header>

    <el-main class="main-body">
      <!-- ================= 学生端视图 ================= -->
      <div v-if="currentRole === 'STUDENT'">
        <el-alert
            title="系统规则提醒：上传附件后将暂存于【草稿箱】（教师不可见）。请在截止前点击【确认提交】，截止期内支持最多 3 次修改重传。若截止仍未手动确认，系统将自动提交草稿。"
            type="info"
            show-icon
            :closable="false"
            style="margin-bottom: 20px;"
        />

        <!-- 实验任务列表 -->
        <el-row :gutter="20">
          <el-col :span="24" v-for="task in taskList" :key="task.id" style="margin-bottom: 20px;">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span style="font-weight: bold; font-size: 16px;">{{ task.title }}</span>
                  <el-tag type="danger">满分: {{ task.maxScore }}分</el-tag>
                </div>
              </template>
              <p style="color: #444; font-size: 14px; line-height: 1.6;"><strong>实验任务要求：</strong>{{ task.content }}</p>
              <p style="color: #888; font-size: 13px;"><strong>截止提交时间：</strong>{{ formatTime(task.deadline) }}</p>

              <!-- 上传到草稿箱区域 (已移除 limit 限制，解决卡顿无反应问题) -->
              <div style="margin-top: 15px; display: flex; align-items: center; gap: 15px;">
                <el-upload
                    action="http://localhost:8080/api/lab/upload"
                    :before-upload="() => handleBeforeUpload(task.id)"
                    :on-success="(res, file) => handleUploadSuccess(res, file, task.id)"
                    :on-error="handleUploadError"
                    :show-file-list="false"
                >
                  <el-button
                      type="primary"
                      :icon="Upload"
                      :loading="uploadingTaskId === task.id"
                  >
                    {{ uploadingTaskId === task.id ? '正在上传中...' : (getSubmission(task.id) ? '重新上传/更新草稿' : '上传报告至草稿箱') }}
                  </el-button>
                </el-upload>

                <span v-if="getSubmission(task.id)" style="font-size: 13px; color: #606266;">
                  当前暂存文件：<strong style="color: #409EFF;">{{ getSubmission(task.id).fileName }}</strong>
                </span>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 我的提交记录与草稿箱 -->
        <el-card shadow="never" style="margin-top: 10px;">
          <template #header>
            <span style="font-weight: bold;">我的实验报告提交记录与草稿管理</span>
          </template>
          <el-table :data="mySubmissions" stripe style="width: 100%">
            <el-table-column prop="taskTitle" label="实验任务" />
            <el-table-column label="附件文件">
              <template #default="{ row }">
                <el-link type="primary" :href="'http://localhost:8080' + row.fileUrl" target="_blank">
                  {{ row.fileName }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="更新/提交时间" width="170">
              <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="140">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'DRAFT'" type="info">草稿箱 (待确认)</el-tag>
                <el-tag v-else-if="row.status === 'SUBMITTED'" type="warning">已正式提交</el-tag>
                <el-tag v-else-if="row.status === 'GRADED'" type="success">已批阅</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="已用修改次数" width="130">
              <template #default="{ row }">
                <span>{{ row.submitCount }} / 3 次</span>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="成绩" width="100">
              <template #default="{ row }">
                <span v-if="row.score !== null" style="color: #67C23A; font-weight: bold;">{{ row.score }} 分</span>
                <span v-else style="color: #909399;">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="feedback" label="教师评语" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <!-- 只要是草稿，或者正式提交后次数 < 3，都可以确认/重交 -->
                <el-button
                    v-if="row.status === 'DRAFT'"
                    type="success"
                    size="small"
                    @click="confirmFinalSubmit(row.taskId)"
                >
                  确认正式提交
                </el-button>
                <el-button
                    v-else-if="row.status === 'SUBMITTED' && row.submitCount < 3"
                    type="primary"
                    plain
                    size="small"
                    @click="promptReupload"
                >
                  申请修改重传
                </el-button>
                <span v-else-if="row.submitCount >= 3" style="font-size: 12px; color: #F56C6C;">已达修改上限</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>

      <!-- ================= 教师端视图 ================= -->
      <div v-else>
        <el-alert title="欢迎进入教师管理台：系统已严格隔离学生草稿，此列表仅展示学生正式确认提交的报告。支持一键打包 ZIP。" type="success" show-icon :closable="false" style="margin-bottom: 20px;" />

        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <div style="display: flex; align-items: center; gap: 15px;">
                <span style="font-weight: bold;">查看实验任务：</span>
                <el-select v-model="selectedTaskId" placeholder="选择任务" style="width: 280px;" @change="loadTaskSubmissions">
                  <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
                </el-select>
              </div>
              <div>
                <el-button type="success" :icon="Plus" @click="taskDialogVisible = true">发布新实验任务</el-button>
                <el-button type="warning" :icon="Download" @click="downloadAllZip">打包下载全部报告 (ZIP)</el-button>
                <el-button type="primary" size="default" @click="loadTaskSubmissions">刷新列表</el-button>
              </div>
            </div>
          </template>

          <el-table :data="teacherSubmissions" border style="width: 100%">
            <el-table-column prop="studentName" label="提交学生" width="140" />
            <el-table-column label="报告附件 (点击下载/预览)">
              <template #default="{ row }">
                <el-link type="primary" :href="'http://localhost:8080' + row.fileUrl" target="_blank">
                  {{ row.fileName }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="修改版本" width="110">
              <template #default="{ row }">第 {{ row.submitCount }} 版</template>
            </el-table-column>
            <el-table-column label="批阅状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'GRADED'" type="success">已批阅</el-tag>
                <el-tag v-else type="danger">待批阅</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="当前成绩" width="120">
              <template #default="{ row }">
                <span v-if="row.score !== null" style="font-weight: bold; color: #409EFF;">{{ row.score }} 分</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="feedback" label="教师评语" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="openGradeDialog(row)">在线打分/评语</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 教师发布新任务弹窗 -->
        <el-dialog v-model="taskDialogVisible" title="发布新实验任务" width="550px">
          <el-form label-width="110px">
            <el-form-item label="实验标题">
              <el-input v-model="newTaskForm.title" placeholder="如：实验三：Vue组件化开发" />
            </el-form-item>
            <el-form-item label="满分值">
              <el-input-number v-model="newTaskForm.maxScore" :min="10" :max="100" />
            </el-form-item>
            <el-form-item label="截止时间">
              <el-date-picker
                  v-model="newTaskForm.deadline"
                  type="datetime"
                  placeholder="选择截止日期与时间"
                  value-format="YYYY-MM-DDTHH:mm:ss"
                  style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="实验要求内容">
              <el-input v-model="newTaskForm.content" type="textarea" :rows="4" placeholder="请详细输入实验要求..." />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="taskDialogVisible = false">取消</el-button>
            <el-button type="success" @click="submitNewTask">确认发布实验</el-button>
          </template>
        </el-dialog>

        <!-- 批阅打分弹窗 -->
        <el-dialog v-model="dialogVisible" title="实验报告在线批阅与评分" width="500px">
          <el-form label-width="90px">
            <el-form-item label="学生姓名">
              <el-input v-model="currentGradeItem.studentName" disabled />
            </el-form-item>
            <el-form-item label="报告文件">
              <el-input v-model="currentGradeItem.fileName" disabled />
            </el-form-item>
            <el-form-item label="评分 (0-100)">
              <el-input-number v-model="gradeForm.score" :min="0" :max="100" />
            </el-form-item>
            <el-form-item label="教师评语">
              <el-input v-model="gradeForm.feedback" type="textarea" :rows="4" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitGrade">提交批阅成绩</el-button>
          </template>
        </el-dialog>
      </div>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Reading, Upload, Plus, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/lab'

const currentRole = ref('STUDENT')
const taskList = ref([])
const selectedTaskId = ref(null)
const mySubmissions = ref([])
const teacherSubmissions = ref([])

// 加载状态控制
const uploadingTaskId = ref(null)

const dialogVisible = ref(false)
const taskDialogVisible = ref(false)
const currentGradeItem = ref({})
const gradeForm = ref({ score: 90, feedback: '' })
const newTaskForm = ref({
  title: '',
  content: '',
  deadline: '2026-10-20T23:59:59',
  maxScore: 100
})

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ')
}

const getSubmission = (taskId) => {
  return mySubmissions.value.find(s => s.taskId === taskId)
}

const switchRole = () => {
  if (currentRole.value === 'STUDENT') {
    loadMySubmissions()
  } else {
    loadTaskSubmissions()
  }
}

// 1. 加载任务
const loadTasks = async () => {
  try {
    const res = await axios.get(`${API_BASE}/tasks`)
    taskList.value = res.data.data
    if (taskList.value.length > 0 && !selectedTaskId.value) {
      selectedTaskId.value = taskList.value[0].id
    }
  } catch (e) {
    ElMessage.error('获取实验任务失败')
  }
}

// 2. 加载学生提交记录
const loadMySubmissions = async () => {
  try {
    const res = await axios.get(`${API_BASE}/submissions/student/2`)
    mySubmissions.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

// 3. 加载教师端选定任务的正式提交
const loadTaskSubmissions = async () => {
  if (!selectedTaskId.value) return
  try {
    const res = await axios.get(`${API_BASE}/submissions/task/${selectedTaskId.value}`)
    teacherSubmissions.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

// 4. 上传前：开启按钮 Loading
const handleBeforeUpload = (taskId) => {
  uploadingTaskId.value = taskId
  ElMessage.info('文件上传处理中，请稍候...')
}

// 5. 上传成功：存入草稿箱
const handleUploadSuccess = async (response, file, taskId) => {
  uploadingTaskId.value = null
  if (response.code === 200) {
    try {
      const res = await axios.post(`${API_BASE}/draft`, {
        taskId: taskId,
        studentId: 2,
        fileName: file.name,
        fileUrl: response.data,
        fileSize: file.size
      })
      if (res.data.code === 200) {
        ElMessage.success('报告已存入草稿箱！请在下方表格中点击【确认正式提交】。')
        loadMySubmissions()
      } else {
        ElMessage.error(res.data.message)
      }
    } catch (e) {
      ElMessage.error('保存草稿失败')
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 6. 上传失败兜底
const handleUploadError = () => {
  uploadingTaskId.value = null
  ElMessage.error('上传超时或网络异常，请重试')
}

// 7. 正式确认提交
const confirmFinalSubmit = async (taskId) => {
  try {
    const res = await axios.post(`${API_BASE}/confirm-submit`, {
      taskId: taskId,
      studentId: 2
    })
    if (res.data.code === 200) {
      ElMessage.success(res.data.message)
      loadMySubmissions()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('提交失败')
  }
}

const promptReupload = () => {
  ElMessage.info('请在对应实验卡片上点击【重新上传/更新草稿】，上传后再确认提交。')
}

// 8. 教师发布新实验
const submitNewTask = async () => {
  if (!newTaskForm.value.title || !newTaskForm.value.content) {
    ElMessage.warning('请填写完整的实验标题与要求')
    return
  }
  try {
    await axios.post(`${API_BASE}/tasks`, newTaskForm.value)
    ElMessage.success('新实验任务发布成功！')
    taskDialogVisible.value = false
    newTaskForm.value.title = ''
    newTaskForm.value.content = ''
    await loadTasks()
  } catch (e) {
    ElMessage.error('发布任务失败')
  }
}

// 9. 教师打分
const openGradeDialog = (row) => {
  currentGradeItem.value = row
  gradeForm.value.score = row.score || 88
  gradeForm.value.feedback = row.feedback || '实验设计合理，代码架构清晰，功能完整。'
  dialogVisible.value = true
}

const submitGrade = async () => {
  try {
    await axios.post(`${API_BASE}/grade`, {
      id: currentGradeItem.value.id,
      score: gradeForm.value.score,
      feedback: gradeForm.value.feedback,
      teacherId: 1
    })
    ElMessage.success('成绩与评语提交成功！')
    dialogVisible.value = false
    loadTaskSubmissions()
  } catch (e) {
    ElMessage.error('打分提交失败')
  }
}

// 10. 教师一键打包下载全部正式报告
const downloadAllZip = () => {
  if (!selectedTaskId.value) return
  window.open(`${API_BASE}/submissions/task/${selectedTaskId.value}/export-zip`)
}

onMounted(async () => {
  await loadTasks()
  loadMySubmissions()
})
</script>

<style scoped>
.common-layout {
  min-height: 100vh;
  background-color: #f5f7fa;
}
.header {
  background-color: #ffffff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  height: 60px;
}
.logo {
  display: flex;
  align-items: center;
  color: #409EFF;
}
.main-body {
  max-width: 1100px;
  margin: 20px auto;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>