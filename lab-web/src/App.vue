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
        <el-alert title="欢迎进入学生工作台：可查看实验要求、在线上传提交实验报告、查询批阅状态与得分。" type="info" show-icon :closable="false" style="margin-bottom: 20px;" />

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
              <p style="color: #888; font-size: 13px;"><strong>截止提交时间：</strong>{{ task.deadline }}</p>

              <!-- 上传提交区域 -->
              <div style="margin-top: 15px;">
                <el-upload
                    action="http://localhost:8080/api/lab/upload"
                    :on-success="(res) => handleUploadSuccess(res, task.id)"
                    :limit="1"
                    :show-file-list="true"
                >
                  <el-button type="primary" :icon="Upload">上传实验报告附件 (PDF/Word)</el-button>
                </el-upload>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 我的提交记录 -->
        <el-card shadow="never" style="margin-top: 10px;">
          <template #header>
            <span style="font-weight: bold;">我的实验报告提交记录与批阅反馈</span>
          </template>
          <el-table :data="mySubmissions" stripe style="width: 100%">
            <el-table-column prop="taskTitle" label="实验任务" />
            <el-table-column prop="fileName" label="已提交文件" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
            <el-table-column label="批阅状态" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'GRADED'" type="success">已批阅</el-tag>
                <el-tag v-else type="warning">待批阅</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="最终得分" width="120">
              <template #default="{ row }">
                <span v-if="row.score !== null" style="color: #67C23A; font-weight: bold; font-size: 16px;">{{ row.score }} 分</span>
                <span v-else style="color: #909399;">暂未评分</span>
              </template>
            </el-table-column>
            <el-table-column prop="feedback" label="教师评语" />
          </el-table>
        </el-card>
      </div>

      <!-- ================= 教师端视图 ================= -->
      <div v-else>
        <el-alert title="欢迎进入教师管理台：支持实时查看学生提交的报告材料、在线打分并录入反馈评语。" type="success" show-icon :closable="false" style="margin-bottom: 20px;" />

        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span style="font-weight: bold;">学生报告待审列表 (实验一)</span>
              <el-button type="primary" size="small" @click="loadTaskSubmissions">刷新列表</el-button>
            </div>
          </template>

          <el-table :data="teacherSubmissions" border style="width: 100%">
            <el-table-column prop="studentName" label="提交学生" width="140" />
            <el-table-column prop="fileName" label="报告附件" />
            <el-table-column prop="submitTime" label="提交时间" width="180" />
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

        <!-- 批阅弹窗 -->
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
              <el-input v-model="gradeForm.feedback" type="textarea" :rows="4" placeholder="请输入指导反馈与改进意见..." />
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
import { Reading, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_BASE = 'http://localhost:8080/api/lab'

const currentRole = ref('STUDENT')
const taskList = ref([])
const mySubmissions = ref([])
const teacherSubmissions = ref([])

const dialogVisible = ref(false)
const currentGradeItem = ref({})
const gradeForm = ref({ score: 90, feedback: '' })

const switchRole = () => {
  if (currentRole.value === 'STUDENT') {
    loadMySubmissions()
  } else {
    loadTaskSubmissions()
  }
}

// 1. 加载实验任务列表
const loadTasks = async () => {
  try {
    const res = await axios.get(`${API_BASE}/tasks`)
    taskList.value = res.data.data
  } catch (e) {
    ElMessage.error('获取实验任务失败，请确认后端 8080 端口已启动')
  }
}

// 2. 加载学生自己的提交历史 (李同学 ID: 2)
const loadMySubmissions = async () => {
  try {
    const res = await axios.get(`${API_BASE}/submissions/student/2`)
    mySubmissions.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

// 3. 加载教师端所有学生提交 (任务 ID: 1)
const loadTaskSubmissions = async () => {
  try {
    const res = await axios.get(`${API_BASE}/submissions/task/1`)
    teacherSubmissions.value = res.data.data
  } catch (e) {
    console.error(e)
  }
}

// 4. 学生上传报告文件成功
const handleUploadSuccess = async (response, taskId) => {
  if (response.code === 200) {
    ElMessage.success('附件上传成功，正在记录提交信息...')
    await axios.post(`${API_BASE}/submit`, {
      taskId: taskId,
      studentId: 2,
      fileName: '李同学_Java实战实验报告.pdf',
      fileUrl: response.data,
      fileSize: 102400
    })
    ElMessage.success('实验报告提交成功！')
    loadMySubmissions()
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 5. 打开打分弹窗
const openGradeDialog = (row) => {
  currentGradeItem.value = row
  gradeForm.value.score = row.score || 88
  gradeForm.value.feedback = row.feedback || '实验设计合理，代码架构清晰，功能完整。'
  dialogVisible.value = true
}

// 6. 提交打分
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

onMounted(() => {
  loadTasks()
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