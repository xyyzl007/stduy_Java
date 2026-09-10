<template>
  <div class="system-container">
    <!-- ================= 1. 未登录：现代科技感居中登录/注册卡片 ================= -->
    <div v-if="!currentUser" class="login-wrapper">
      <div class="login-box">
        <div class="login-header">
          <div class="icon-badge"><el-icon :size="32" color="#ffffff"><Reading /></el-icon></div>
          <h2 class="title">学生实验报告提交与批阅系统</h2>
          <p class="subtitle">江苏科技大学 · Java后端开发实战课程集中实践系统</p>
        </div>

        <el-tabs v-model="authTab" stretch class="custom-tabs">
          <el-tab-pane label="系统登录" name="login">
            <el-form :model="loginForm" label-position="top" style="margin-top: 15px;">
              <el-form-item label="登录账号 (学号 / 工号)">
                <el-input v-model="loginForm.username" :prefix-icon="UserFilled" placeholder="如: S2026001 / T2026001" size="large" />
              </el-form-item>
              <el-form-item label="系统密码">
                <el-input v-model="loginForm.password" :prefix-icon="Lock" type="password" show-password placeholder="请输入密码" size="large" />
              </el-form-item>
              <el-button type="primary" size="large" class="submit-btn" @click="handleLogin">登录工作台</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册新账号" name="register">
            <el-form :model="regForm" label-position="top" style="margin-top: 10px;">
              <el-form-item label="身份类型">
                <el-radio-group v-model="regForm.role">
                  <el-radio-button label="ROLE_STUDENT">学生</el-radio-button>
                  <el-radio-button label="ROLE_TEACHER">教师</el-radio-button>
                </el-radio-group>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="学号/工号"><el-input v-model="regForm.username" placeholder="账号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="真实姓名"><el-input v-model="regForm.realName" placeholder="姓名" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="登录密码"><el-input v-model="regForm.password" type="password" show-password placeholder="密码" /></el-form-item>
              <el-form-item v-if="regForm.role === 'ROLE_STUDENT'" label="所属行政班级">
                <el-select v-model="regForm.classId" placeholder="选择班级" style="width: 100%;">
                  <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
                </el-select>
              </el-form-item>
              <el-row :gutter="12">
                <el-col :span="12"><el-form-item label="联系电话"><el-input v-model="regForm.phone" placeholder="手机号" /></el-form-item></el-col>
                <el-col :span="12"><el-form-item label="电子邮箱"><el-input v-model="regForm.email" placeholder="邮箱" /></el-form-item></el-col>
              </el-row>
              <el-button type="success" size="large" class="submit-btn" @click="handleRegister">确认注册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- ================= 2. 已登录：全屏自适应主系统 ================= -->
    <div v-else class="app-layout">
      <el-header class="nav-header">
        <div class="brand">
          <div class="brand-icon"><el-icon :size="22" color="#ffffff"><Reading /></el-icon></div>
          <div class="brand-text">
            <span class="brand-title">学生实验报告提交与批阅系统</span>
            <span class="brand-sub">Jiangsu University of Science and Technology · 教学实战平台</span>
          </div>
        </div>
        <div class="user-panel">
          <div class="user-pill">
            <span class="role-badge" :class="currentUser.role === 'ROLE_TEACHER' ? 'bg-tag-teacher' : 'bg-tag-student'">
              {{ currentUser.role === 'ROLE_TEACHER' ? '授课教师' : '在籍学生' }}
            </span>
            <span class="uname">{{ currentUser.realName }}</span>
            <span class="uid">({{ currentUser.username }})</span>
          </div>
          <el-button round size="small" class="nav-action-btn" :icon="UserFilled" @click="profileDialogVisible = true">个人中心</el-button>
          <el-button round size="small" type="danger" plain class="nav-logout-btn" :icon="SwitchButton" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <main class="main-content">
        <!-- 学生端视图 -->
        <div v-if="currentUser.role === 'ROLE_STUDENT'">
          <el-row :gutter="18" class="metric-row">
            <el-col :span="6">
              <div class="metric-card card-cyan">
                <div class="metric-text"><div class="num">{{ taskList.length }}</div><div class="txt">学期实验总数</div></div>
                <div class="icon-wrap"><el-icon><Tickets /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-amber">
                <div class="metric-text"><div class="num">{{ countDrafts }}</div><div class="txt">草稿箱待确认</div></div>
                <div class="icon-wrap"><el-icon><EditPen /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-blue">
                <div class="metric-text"><div class="num">{{ countSubmitted }}</div><div class="txt">已正式提交</div></div>
                <div class="icon-wrap"><el-icon><CircleCheck /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-emerald">
                <div class="metric-text"><div class="num">{{ studentAvgScore }}</div><div class="txt">实验平均分</div></div>
                <div class="icon-wrap"><el-icon><Trophy /></el-icon></div>
              </div>
            </el-col>
          </el-row>

          <div class="tips-banner">
            <el-icon :size="18"><InfoFilled /></el-icon>
            <span><strong>提交机制提醒：</strong>附件上传后暂存于【草稿箱】（教师不可见）。核实无误后请点击【确认提交】，截止日前限期修改 3 次，超期系统自动提交草稿。</span>
          </div>

          <!-- 任务列表 -->
          <div class="content-block">
            <div class="block-header">
              <span class="title"><el-icon><Document /></el-icon> 实验任务专区</span>
              <span class="desc">请按各课程要求撰写并上传实验报告附件</span>
            </div>

            <el-row :gutter="20">
              <el-col :span="12" v-for="task in taskList" :key="task.id" style="margin-bottom: 20px;">
                <div class="task-card">
                  <div class="task-card-top">
                    <span class="t-name">{{ task.title }}</span>
                    <span class="max-score-badge">满分 {{ task.maxScore }} 分</span>
                  </div>
                  <div class="t-body">
                    <p><strong>实验要求：</strong>{{ task.content }}</p>
                    <div class="t-time"><el-icon><Calendar /></el-icon> 截止时间：{{ formatTime(task.deadline) }}</div>
                  </div>
                  <div class="task-card-footer">
                    <el-upload
                        action="http://localhost:8080/api/lab/upload"
                        :before-upload="() => handleBeforeUpload(task.id)"
                        :on-success="(res, file) => handleUploadSuccess(res, file, task.id)"
                        :show-file-list="false"
                    >
                      <el-button type="primary" :icon="Upload" :loading="uploadingTaskId === task.id">
                        {{ uploadingTaskId === task.id ? '正在上传...' : (getSubmission(task.id) ? '重新上传/更新草稿' : '上传报告至草稿箱') }}
                      </el-button>
                    </el-upload>
                    <div v-if="getSubmission(task.id)" class="file-tag">
                      已暂存: <span>{{ getSubmission(task.id).fileName }}</span>
                    </div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>

          <!-- 提交表格 -->
          <div class="content-block" style="margin-top: 20px;">
            <div class="block-header">
              <span class="title"><el-icon><FolderOpened /></el-icon> 我的实验提交记录与批改反馈</span>
              <el-button size="small" :icon="Refresh" @click="loadMySubmissions">刷新列表</el-button>
            </div>

            <el-table :data="mySubmissions" stripe border style="width: 100%" class="custom-table">
              <el-table-column prop="taskTitle" label="实验任务" min-width="180" />
              <el-table-column label="附件报告" min-width="220">
                <template #default="{ row }">
                  <el-link type="primary" :href="'http://localhost:8080' + row.fileUrl" target="_blank" :icon="Document">
                    {{ row.fileName }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="submitTime" label="更新/提交时间" width="170">
                <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
              </el-table-column>
              <el-table-column label="流转状态" width="140">
                <template #default="{ row }">
                  <span v-if="row.status === 'DRAFT'" class="badge badge-gray">草稿箱 (待确认)</span>
                  <span v-else-if="row.status === 'SUBMITTED'" class="badge badge-yellow">已正式提交</span>
                  <span v-else-if="row.status === 'GRADED'" class="badge badge-green">已完成批阅</span>
                </template>
              </el-table-column>
              <el-table-column label="已用修改次数" width="140">
                <template #default="{ row }">
                  <el-progress :percentage="(row.submitCount / 3) * 100" :format="() => `${row.submitCount}/3次`" :status="row.submitCount >= 3 ? 'exception' : ''" />
                </template>
              </el-table-column>
              <el-table-column prop="score" label="量化得分" width="110">
                <template #default="{ row }">
                  <span v-if="row.score !== null" class="score-highlight">{{ row.score }} 分</span>
                  <span v-else style="color: #aaa;">暂无</span>
                </template>
              </el-table-column>
              <el-table-column prop="feedback" label="教师评语" min-width="180" />
              <el-table-column label="操作" width="140" fixed="right">
                <template #default="{ row }">
                  <el-button v-if="row.status === 'DRAFT'" type="success" size="small" @click="confirmFinalSubmit(row.taskId)">确认正式提交</el-button>
                  <span v-else-if="row.submitCount >= 3" style="color: #f56c6c; font-size: 12px;">已达修改上限</span>
                  <span v-else style="color: #409eff; font-size: 12px;">截止前可重传</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 教师端视图 -->
        <div v-else>
          <el-row :gutter="18" class="metric-row">
            <el-col :span="6">
              <div class="metric-card card-cyan">
                <div class="metric-text"><div class="num">{{ taskList.length }}</div><div class="txt">已发布实验任务</div></div>
                <div class="icon-wrap"><el-icon><Tickets /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-amber">
                <div class="metric-text"><div class="num">{{ teacherSubmissions.length }}</div><div class="txt">收到正式报告 (份)</div></div>
                <div class="icon-wrap"><el-icon><Files /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-blue">
                <div class="metric-text"><div class="num">{{ teacherPendingCount }}</div><div class="txt">待批阅人数</div></div>
                <div class="icon-wrap"><el-icon><Clock /></el-icon></div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="metric-card card-emerald">
                <div class="metric-text"><div class="num">{{ teacherGradedRate }}%</div><div class="txt">整体批改完成率</div></div>
                <div class="icon-wrap"><el-icon><Histogram /></el-icon></div>
              </div>
            </el-col>
          </el-row>

          <div class="content-block teacher-box">
            <el-tabs v-model="teacherActiveTab" type="border-card" class="modern-tabs">
              <!-- 标签 1: 实验报告批阅与归档 -->
              <el-tab-pane name="reviews">
                <template #label><span class="tab-title"><el-icon><DocumentChecked /></el-icon> 实验报告批阅与归档</span></template>

                <div class="action-bar">
                  <div class="bar-left">
                    <span class="label-text">筛选实验：</span>
                    <el-select v-model="selectedTaskId" placeholder="选择任务" style="width: 260px;" @change="loadTaskSubmissions">
                      <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
                    </el-select>

                    <el-button v-if="currentTask" type="primary" plain :icon="Edit" size="small" @click="openEditTaskDialog">编辑此实验</el-button>
                    <el-popconfirm v-if="currentTask" title="确定要彻底删除该实验任务吗？关联的学生提交也一并删除！" @confirm="handleDeleteTask">
                      <template #reference>
                        <el-button type="danger" plain :icon="Delete" size="small">删除此实验</el-button>
                      </template>
                    </el-popconfirm>
                  </div>
                  <div class="bar-right">
                    <el-button type="success" :icon="Plus" @click="openCreateTaskDialog">发布新实验</el-button>
                    <el-button type="warning" :icon="Download" @click="downloadAllZip">打包下载全部报告 (ZIP)</el-button>
                    <el-button type="primary" :icon="Refresh" @click="loadTaskSubmissions">刷新列表</el-button>
                  </div>
                </div>

                <el-table :data="teacherSubmissions" border stripe style="width: 100%" class="custom-table">
                  <el-table-column prop="studentName" label="提交学生" width="130" />
                  <el-table-column label="报告附件 (点击下载/预览)" min-width="240">
                    <template #default="{ row }">
                      <el-link type="primary" :href="'http://localhost:8080' + row.fileUrl" target="_blank" :icon="Document">
                        {{ row.fileName }}
                      </el-link>
                    </template>
                  </el-table-column>
                  <el-table-column prop="submitTime" label="提交时间" width="170">
                    <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
                  </el-table-column>
                  <el-table-column label="修改版本" width="100">
                    <template #default="{ row }">第 {{ row.submitCount }} 版</template>
                  </el-table-column>
                  <el-table-column label="批阅状态" width="110">
                    <template #default="{ row }">
                      <el-tag v-if="row.status === 'GRADED'" type="success" effect="dark">已批阅</el-tag>
                      <el-tag v-else type="danger" effect="dark">待批阅</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="量化得分" width="110">
                    <template #default="{ row }">
                      <span v-if="row.score !== null" class="score-highlight">{{ row.score }} 分</span>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="feedback" label="教师评语" min-width="180" />
                  <el-table-column label="操作" width="130" fixed="right">
                    <template #default="{ row }">
                      <el-button type="primary" size="small" :icon="Edit" @click="openGradeDialog(row)">在线打分</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 标签 2: 班级管理 -->
              <el-tab-pane name="classes">
                <template #label><span class="tab-title"><el-icon><School /></el-icon> 行政班级管理</span></template>
                <div class="action-bar">
                  <span class="label-text">已开设行政班级名录</span>
                  <el-button type="primary" :icon="Plus" @click="classDialogVisible = true">新建行政班级</el-button>
                </div>
                <el-table :data="classList" border stripe style="width: 100%" class="custom-table">
                  <el-table-column prop="id" label="班级ID" width="100" />
                  <el-table-column prop="className" label="班级全称" min-width="220" />
                  <el-table-column prop="gradeYear" label="所属年级" width="160" />
                  <el-table-column prop="createdAt" label="创建时间" width="200">
                    <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
                  </el-table-column>
                  <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                      <el-popconfirm title="确定要删除该班级吗？原班级学生将变为【待分配班级】！" @confirm="handleDeleteClass(row.id)">
                        <template #reference>
                          <el-button type="danger" size="small" :icon="Delete">删除班级</el-button>
                        </template>
                      </el-popconfirm>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 标签 3: 课程管理 -->
              <el-tab-pane name="courses">
                <template #label><span class="tab-title"><el-icon><Management /></el-icon> 授课课程管理</span></template>
                <div class="action-bar">
                  <span class="label-text">主讲课程开设列表</span>
                  <el-button type="primary" :icon="Plus" @click="courseDialogVisible = true">开设新课程</el-button>
                </div>
                <el-table :data="courseList" border stripe style="width: 100%" class="custom-table">
                  <el-table-column prop="courseCode" label="课程代码" width="140" />
                  <el-table-column prop="courseName" label="课程全称" min-width="220" />
                  <el-table-column prop="teacherName" label="主讲教师" width="140" />
                  <el-table-column prop="semester" label="开课学期" width="160" />
                  <el-table-column prop="description" label="课程大纲描述" min-width="240" />
                  <el-table-column label="操作" width="120" fixed="right">
                    <template #default="{ row }">
                      <el-popconfirm title="确定要彻底删除该课程吗？该课程下所有实验也将被同步清理！" @confirm="handleDeleteCourse(row.id)">
                        <template #reference>
                          <el-button type="danger" size="small" :icon="Delete">删除课程</el-button>
                        </template>
                      </el-popconfirm>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>

              <!-- 标签 4: 学生名单管理 (已集成【全部学生】、【待分配班级】及【一键重置】) -->
              <el-tab-pane name="students">
                <template #label><span class="tab-title"><el-icon><User /></el-icon> 学生名单管理</span></template>
                <div class="action-bar">
                  <div class="bar-left" style="gap: 15px;">
                    <div>
                      <span class="label-text">筛选班级：</span>
                      <el-select v-model="filterClassId" placeholder="选择班级范围" style="width: 220px;" @change="loadStudentList">
                        <el-option label="-- 全部在籍学生 --" :value="null" />
                        <el-option label="-- 待分配班级学生 --" :value="-1" />
                        <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
                      </el-select>
                    </div>
                    <div>
                      <span class="label-text">快速搜索：</span>
                      <el-input
                          v-model="searchStudentKey"
                          placeholder="输入学号或姓名搜索"
                          style="width: 220px;"
                          clearable
                          @clear="loadStudentList"
                          @keyup.enter="loadStudentList"
                      />
                    </div>
                    <el-button type="primary" :icon="Refresh" @click="loadStudentList">查询名单</el-button>
                    <el-button :icon="RefreshRight" @click="resetStudentFilter">重置</el-button>
                  </div>
                </div>

                <el-table :data="studentList" border stripe style="width: 100%" class="custom-table">
                  <el-table-column prop="username" label="学号" width="150" />
                  <el-table-column prop="realName" label="学生姓名" width="150" />
                  <el-table-column label="所属行政班级" min-width="220">
                    <template #default="{ row }">
                      <el-tag v-if="row.className" type="success">{{ row.className }}</el-tag>
                      <el-tag v-else type="warning" effect="dark">待重新分配班级</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="phone" label="联系电话" width="160" />
                  <el-table-column prop="email" label="联系邮箱" min-width="220" />
                  <el-table-column label="操作" width="140" fixed="right">
                    <template #default="{ row }">
                      <el-button type="primary" plain size="small" :icon="Edit" @click="openAssignClassDialog(row)">分配班级</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>
      </main>

      <footer class="app-footer">
        <p>江苏科技大学 · Java后端开发实战课程集中实践项目</p>
        <p class="tech-stack">架构支持: Spring Boot 3.2.5 + Vue 3 + Element Plus + MySQL 8.x | 仓库管理: Gitee</p>
      </footer>
    </div>

    <!-- 弹窗：发布 / 编辑实验任务 -->
    <el-dialog v-model="taskDialogVisible" :title="isEditingTask ? '编辑修改实验任务' : '发布新实验任务'" width="560px">
      <el-form label-width="110px">
        <el-form-item label="所属课程">
          <el-select v-model="newTaskForm.courseId" placeholder="请选择该实验所属的主讲课程" style="width: 100%;">
            <el-option v-for="c in courseList" :key="c.id" :label="c.courseName + ' (' + c.courseCode + ')'" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实验标题"><el-input v-model="newTaskForm.title" placeholder="如：实验三：Vue组件化实战" /></el-form-item>
        <el-form-item label="考核满分"><el-input-number v-model="newTaskForm.maxScore" :min="10" :max="100" /></el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker v-model="newTaskForm.deadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="实验要求内容"><el-input v-model="newTaskForm.content" type="textarea" :rows="4" placeholder="请详细输入实验要求..." /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitTaskForm">{{ isEditingTask ? '保存修改' : '确认发布任务' }}</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：老师为学生调整/分配班级 -->
    <el-dialog v-model="assignClassDialogVisible" title="为学生分配/调整行政班级" width="460px">
      <el-form label-width="90px">
        <el-form-item label="学生姓名"><el-input v-model="currentStudent.realName" disabled /></el-form-item>
        <el-form-item label="学生学号"><el-input v-model="currentStudent.username" disabled /></el-form-item>
        <el-form-item label="目标班级">
          <el-select v-model="targetClassId" placeholder="请选择调入的行政班级" style="width: 100%;">
            <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignClassDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignClass">确认分配</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：在线打分 -->
    <el-dialog v-model="dialogVisible" title="实验报告在线批阅与评分" width="560px">
      <el-form label-width="90px">
        <el-form-item label="学生姓名"><el-input v-model="currentGradeItem.studentName" disabled /></el-form-item>
        <el-form-item label="报告附件"><el-input v-model="currentGradeItem.fileName" disabled /></el-form-item>
        <el-form-item label="量化得分">
          <el-input-number v-model="gradeForm.score" :min="0" :max="100" size="large" style="width: 180px;" />
          <span style="margin-left: 10px; color: #888;">分 (满分100)</span>
        </el-form-item>
        <el-form-item label="快捷评语">
          <div class="quick-tags">
            <el-tag class="tag-click" @click="appendFeedback('代码架构规范，逻辑严谨，实验完成度高。')">+ 优秀模板</el-tag>
            <el-tag class="tag-click" type="warning" @click="appendFeedback('实验步骤完整，但缺少必要的异常处理，建议完善。')">+ 改进建议</el-tag>
            <el-tag class="tag-click" type="danger" @click="appendFeedback('报告排版较为凌乱，缺少关键结果截图，请注意规范。')">+ 规范提醒</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="指导评语">
          <el-input v-model="gradeForm.feedback" type="textarea" :rows="4" placeholder="请输入指导评语..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrade">提交批阅成绩</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：个人中心 -->
    <el-dialog v-model="profileDialogVisible" title="个人中心 (资料与安全)" width="500px">
      <el-tabs v-model="profileTab">
        <el-tab-pane label="基本资料编辑" name="info">
          <el-form :model="profileForm" label-width="90px">
            <el-form-item label="学号/工号"><el-input v-model="profileForm.username" disabled /></el-form-item>
            <el-form-item label="真实姓名"><el-input v-model="profileForm.realName" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="profileForm.phone" /></el-form-item>
            <el-form-item label="联系邮箱"><el-input v-model="profileForm.email" /></el-form-item>
            <el-button type="primary" style="margin-left: 90px;" @click="updateProfile">保存资料</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="登录密码修改" name="pwd">
          <el-form :model="pwdForm" label-width="90px">
            <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
            <el-button type="danger" style="margin-left: 90px;" @click="updatePassword">确认修改</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 弹窗：新建班级与课程 -->
    <el-dialog v-model="classDialogVisible" title="开设新行政班级" width="450px">
      <el-form label-width="90px">
        <el-form-item label="班级全称"><el-input v-model="newClassForm.className" placeholder="如: 2023级软件工程1班" /></el-form-item>
        <el-form-item label="所属年级"><el-input v-model="newClassForm.gradeYear" placeholder="如: 2023级" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="classDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewClass">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="courseDialogVisible" title="开设新主讲课程" width="500px">
      <el-form label-width="90px">
        <el-form-item label="课程代码"><el-input v-model="newCourseForm.courseCode" placeholder="如: CS301" /></el-form-item>
        <el-form-item label="课程名称"><el-input v-model="newCourseForm.courseName" placeholder="如: 软件工程项目实战" /></el-form-item>
        <el-form-item label="开课学期"><el-input v-model="newCourseForm.semester" placeholder="如: 2026-秋季" /></el-form-item>
        <el-form-item label="课程简介"><el-input v-model="newCourseForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewCourse">确认开设</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Reading, Upload, Plus, Download, UserFilled, Lock, SwitchButton,
  Document, EditPen, CircleCheck, Trophy, Tickets, Files, Clock, Histogram,
  FolderOpened, Edit, Delete, School, Management, User, Refresh, RefreshRight, Calendar, InfoFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_LAB = 'http://localhost:8080/api/lab'
const API_AUTH = 'http://localhost:8080/api/auth'
const API_MANAGE = 'http://localhost:8080/api/manage'

// 基础状态
const currentUser = ref(null)
const authTab = ref('login')
const loginForm = ref({ username: 'T2026001', password: '123456' })
const regForm = ref({ role: 'ROLE_STUDENT', username: '', realName: '', password: '', classId: null, phone: '', email: '' })

// 个人中心
const profileDialogVisible = ref(false)
const profileTab = ref('info')
const profileForm = ref({})
const pwdForm = ref({ oldPassword: '', newPassword: '' })

// 教师端状态
const teacherActiveTab = ref('reviews')
const taskList = ref([])
const selectedTaskId = ref(null)
const mySubmissions = ref([])
const teacherSubmissions = ref([])
const classList = ref([])
const courseList = ref([])
const studentList = ref([])
const filterClassId = ref(null)
const searchStudentKey = ref('')

// 弹窗状态
const uploadingTaskId = ref(null)
const dialogVisible = ref(false)
const taskDialogVisible = ref(false)
const isEditingTask = ref(false)
const classDialogVisible = ref(false)
const courseDialogVisible = ref(false)
const assignClassDialogVisible = ref(false)

const currentStudent = ref({})
const targetClassId = ref(null)

const currentGradeItem = ref({})
const gradeForm = ref({ score: 90, feedback: '' })
const newTaskForm = ref({ id: null, courseId: null, title: '', content: '', deadline: '2026-10-30T23:59:59', maxScore: 100 })
const newClassForm = ref({ className: '', gradeYear: '2023级' })
const newCourseForm = ref({ courseCode: '', courseName: '', semester: '2026-秋季', description: '' })

const currentTask = computed(() => taskList.value.find(t => t.id === selectedTaskId.value))
const countDrafts = computed(() => mySubmissions.value.filter(s => s.status === 'DRAFT').length)
const countSubmitted = computed(() => mySubmissions.value.filter(s => s.status === 'SUBMITTED').length)
const studentAvgScore = computed(() => {
  const graded = mySubmissions.value.filter(s => s.score !== null)
  if (!graded.length) return '暂无'
  const sum = graded.reduce((acc, cur) => acc + Number(cur.score), 0)
  return (sum / graded.length).toFixed(1)
})
const teacherPendingCount = computed(() => teacherSubmissions.value.filter(s => s.status !== 'GRADED').length)
const teacherGradedRate = computed(() => {
  if (!teacherSubmissions.value.length) return 0
  const graded = teacherSubmissions.value.filter(s => s.status === 'GRADED').length
  return Math.round((graded / teacherSubmissions.value.length) * 100)
})

const formatTime = (t) => t ? t.replace('T', ' ') : '-'
const getSubmission = (taskId) => mySubmissions.value.find(s => s.taskId === taskId)
const appendFeedback = (text) => { gradeForm.value.feedback = text }

// 登录/注册
const handleLogin = async () => {
  try {
    const res = await axios.post(`${API_AUTH}/login`, loginForm.value)
    if (res.data.code === 200) {
      currentUser.value = res.data.data
      profileForm.value = { ...res.data.data }
      ElMessage.success(`欢迎进入系统，${currentUser.value.realName}！`)
      initDashboard()
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) { ElMessage.error('登录连接失败，请确认后端已启动') }
}

const handleRegister = async () => {
  try {
    const res = await axios.post(`${API_AUTH}/register`, regForm.value)
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录！')
      authTab.value = 'login'
      loginForm.value.username = regForm.value.username
      loginForm.value.password = regForm.value.password
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) { ElMessage.error('注册请求失败') }
}

const handleLogout = () => {
  currentUser.value = null
  ElMessage.info('已安全退出')
}

// 个人中心
const updateProfile = async () => {
  const res = await axios.post(`${API_AUTH}/profile`, profileForm.value)
  if (res.data.code === 200) {
    currentUser.value = res.data.data
    ElMessage.success('资料已保存！')
    profileDialogVisible.value = false
  }
}

const updatePassword = async () => {
  const res = await axios.post(`${API_AUTH}/change-password`, {
    userId: currentUser.value.id,
    oldPassword: pwdForm.value.oldPassword,
    newPassword: pwdForm.value.newPassword
  })
  if (res.data.code === 200) {
    ElMessage.success('密码修改成功！')
    pwdForm.value = { oldPassword: '', newPassword: '' }
    profileDialogVisible.value = false
  } else {
    ElMessage.error(res.data.message)
  }
}

// 数据加载
const initDashboard = async () => {
  await loadTasks()
  loadClasses()
  loadCourses()
  if (currentUser.value.role === 'ROLE_STUDENT') {
    loadMySubmissions()
  } else {
    loadTaskSubmissions()
    loadStudentList()
  }
}

const loadTasks = async () => {
  const res = await axios.get(`${API_LAB}/tasks`)
  taskList.value = res.data.data
  if (taskList.value.length > 0) {
    if (!selectedTaskId.value || !taskList.value.some(t => t.id === selectedTaskId.value)) {
      selectedTaskId.value = taskList.value[0].id
    }
  } else {
    selectedTaskId.value = null
  }
}

const loadMySubmissions = async () => {
  const res = await axios.get(`${API_LAB}/submissions/student/${currentUser.value.id}`)
  mySubmissions.value = res.data.data
}

const loadTaskSubmissions = async () => {
  if (!selectedTaskId.value) {
    teacherSubmissions.value = []
    return
  }
  const res = await axios.get(`${API_LAB}/submissions/task/${selectedTaskId.value}`)
  teacherSubmissions.value = res.data.data
}

const loadClasses = async () => {
  const res = await axios.get(`${API_MANAGE}/classes`)
  classList.value = res.data.data
}

const loadCourses = async () => {
  const res = await axios.get(`${API_MANAGE}/courses`)
  courseList.value = res.data.data
}

// 核心查询函数
const loadStudentList = async () => {
  let url = `${API_MANAGE}/students?`
  if (filterClassId.value !== null && filterClassId.value !== '' && filterClassId.value !== undefined) {
    url += `classId=${filterClassId.value}&`
  }
  if (searchStudentKey.value && searchStudentKey.value.trim()) {
    url += `keyword=${encodeURIComponent(searchStudentKey.value.trim())}&`
  }
  const res = await axios.get(url)
  studentList.value = res.data.data
}

// 一键重置函数 (恢复查看全部学生)
const resetStudentFilter = () => {
  filterClassId.value = null
  searchStudentKey.value = ''
  loadStudentList()
}

// 任务管理
const openCreateTaskDialog = () => {
  isEditingTask.value = false
  newTaskForm.value = {
    id: null,
    courseId: courseList.value.length > 0 ? courseList.value[0].id : 1,
    title: '',
    content: '',
    deadline: '2026-10-30T23:59:59',
    maxScore: 100
  }
  taskDialogVisible.value = true
}

const openEditTaskDialog = () => {
  if (!currentTask.value) return
  isEditingTask.value = true
  newTaskForm.value = {
    id: currentTask.value.id,
    courseId: currentTask.value.courseId || 1,
    title: currentTask.value.title,
    content: currentTask.value.content,
    deadline: currentTask.value.deadline,
    maxScore: currentTask.value.maxScore
  }
  taskDialogVisible.value = true
}

const submitTaskForm = async () => {
  if (!newTaskForm.value.title || !newTaskForm.value.content) {
    ElMessage.warning('请填写完整的实验标题与要求')
    return
  }
  if (isEditingTask.value) {
    await axios.put(`${API_LAB}/tasks`, newTaskForm.value)
    ElMessage.success('实验任务修改成功！')
  } else {
    await axios.post(`${API_LAB}/tasks`, newTaskForm.value)
    ElMessage.success('新实验发布成功！')
  }
  taskDialogVisible.value = false
  isEditingTask.value = false
  await loadTasks()
  loadTaskSubmissions()
}

const handleDeleteTask = async () => {
  if (!selectedTaskId.value) return
  await axios.delete(`${API_LAB}/tasks/${selectedTaskId.value}`)
  ElMessage.success('该实验任务及关联所有学生提交已彻底删除！')
  selectedTaskId.value = null
  await loadTasks()
  loadTaskSubmissions()
}

// 班级与课程删除
const handleDeleteClass = async (classId) => {
  const res = await axios.delete(`${API_MANAGE}/classes/${classId}`)
  ElMessage.success(res.data.message || '班级已删除！')
  await loadClasses()
  loadStudentList()
}

const handleDeleteCourse = async (courseId) => {
  const res = await axios.delete(`${API_MANAGE}/courses/${courseId}`)
  ElMessage.success(res.data.message || '主讲课程已彻底删除！')
  await loadCourses()
  await loadTasks()
  loadTaskSubmissions()
}

// 分配班级
const openAssignClassDialog = (row) => {
  currentStudent.value = row
  targetClassId.value = row.classId || (classList.value.length > 0 ? classList.value[0].id : null)
  assignClassDialogVisible.value = true
}

const submitAssignClass = async () => {
  if (!targetClassId.value) {
    ElMessage.warning('请选择目标行政班级')
    return
  }
  await axios.post(`${API_MANAGE}/students/assign-class`, {
    studentId: currentStudent.value.id,
    classId: targetClassId.value
  })
  ElMessage.success(`已成功将学生【${currentStudent.value.realName}】分配至目标班级！`)
  assignClassDialogVisible.value = false
  loadStudentList()
}

// 学生上传与确认
const handleBeforeUpload = (taskId) => {
  uploadingTaskId.value = taskId
  ElMessage.info('正在上传文件到草稿箱...')
}

const handleUploadSuccess = async (response, file, taskId) => {
  uploadingTaskId.value = null
  if (response.code === 200) {
    await axios.post(`${API_LAB}/draft`, {
      taskId: taskId,
      studentId: currentUser.value.id,
      fileName: file.name,
      fileUrl: response.data,
      fileSize: file.size
    })
    ElMessage.success('附件已存入草稿箱！核实无误后请点击【确认正式提交】。')
    loadMySubmissions()
  }
}

const confirmFinalSubmit = async (taskId) => {
  const res = await axios.post(`${API_LAB}/confirm-submit`, {
    taskId: taskId,
    studentId: currentUser.value.id
  })
  if (res.data.code === 200) {
    ElMessage.success(res.data.message)
    loadMySubmissions()
  } else {
    ElMessage.error(res.data.message)
  }
}

const submitNewClass = async () => {
  await axios.post(`${API_MANAGE}/classes`, newClassForm.value)
  ElMessage.success('新行政班级开设成功！')
  classDialogVisible.value = false
  newClassForm.value = { className: '', gradeYear: '2023级' }
  loadClasses()
}

const submitNewCourse = async () => {
  newCourseForm.value.teacherId = currentUser.value.id
  await axios.post(`${API_MANAGE}/courses`, newCourseForm.value)
  ElMessage.success('新课程开设成功！')
  courseDialogVisible.value = false
  newCourseForm.value = { courseCode: '', courseName: '', semester: '2026-秋季', description: '' }
  loadCourses()
}

const openGradeDialog = (row) => {
  currentGradeItem.value = row
  gradeForm.value.score = row.score || 88
  gradeForm.value.feedback = row.feedback || '代码设计合理，逻辑严谨，实验完成度高。'
  dialogVisible.value = true
}

const submitGrade = async () => {
  await axios.post(`${API_LAB}/grade`, {
    id: currentGradeItem.value.id,
    score: gradeForm.value.score,
    feedback: gradeForm.value.feedback,
    teacherId: currentUser.value.id
  })
  ElMessage.success('打分与评语提交成功！')
  dialogVisible.value = false
  loadTaskSubmissions()
}

const downloadAllZip = () => {
  if (!selectedTaskId.value) return
  window.open(`${API_LAB}/submissions/task/${selectedTaskId.value}/export-zip`)
}

onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.system-container { min-height: 100vh; background: linear-gradient(180deg, #edf2f7 0%, #e2e8f0 100%); color: #2d3748; }

/* 登录 */
.login-wrapper { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #102a45 0%, #1e3c72 50%, #2a5298 100%); }
.login-box { width: 480px; background: #ffffff; border-radius: 14px; padding: 35px 40px; box-shadow: 0 16px 40px rgba(0, 0, 0, 0.25); }
.login-header { text-align: center; margin-bottom: 20px; }
.icon-badge { width: 60px; height: 60px; background: #1e3c72; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.login-header .title { font-size: 20px; color: #1a202c; margin: 6px 0; }
.login-header .subtitle { font-size: 13px; color: #718096; }
.submit-btn { width: 100%; margin-top: 12px; font-weight: bold; }

/* 导航 */
.nav-header { background: linear-gradient(90deg, #102a45 0%, #1e3c72 100%); color: #ffffff; display: flex; justify-content: space-between; align-items: center; padding: 0 35px; height: 68px; box-shadow: 0 3px 12px rgba(0, 0, 0, 0.15); }
.brand { display: flex; align-items: center; gap: 12px; }
.brand-icon { width: 38px; height: 38px; background: rgba(255, 255, 255, 0.15); border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.brand-text { display: flex; flex-direction: column; }
.brand-title { font-size: 18px; font-weight: bold; letter-spacing: 0.5px; }
.brand-sub { font-size: 11px; opacity: 0.75; }
.user-panel { display: flex; align-items: center; gap: 15px; }
.user-pill { background: rgba(255, 255, 255, 0.1); padding: 5px 14px; border-radius: 20px; font-size: 13.5px; display: flex; align-items: center; gap: 8px; border: 1px solid rgba(255, 255, 255, 0.15); }
.role-badge { padding: 2px 8px; border-radius: 12px; font-size: 11px; font-weight: bold; }
.bg-tag-teacher { background: #f56c6c; color: #fff; }
.bg-tag-student { background: #67c23a; color: #fff; }
.nav-action-btn { background: rgba(255, 255, 255, 0.2); border: none; color: #ffffff; }
.nav-action-btn:hover { background: rgba(255, 255, 255, 0.35); color: #ffffff; }

/* 内容布局 (1560px) */
.main-content { width: 96%; max-width: 1560px; margin: 22px auto 40px auto; padding: 0; }

/* 看板 */
.metric-row { margin-bottom: 22px; }
.metric-card { border-radius: 12px; padding: 20px 24px; color: #ffffff; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08); transition: transform 0.25s ease, box-shadow 0.25s ease; }
.metric-card:hover { transform: translateY(-4px); box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12); }
.metric-text .num { font-size: 30px; font-weight: 800; line-height: 1.1; }
.metric-text .txt { font-size: 13.5px; opacity: 0.92; margin-top: 5px; }
.icon-wrap { font-size: 42px; opacity: 0.65; }
.card-cyan { background: linear-gradient(135deg, #0ba360 0%, #3cba92 100%); }
.card-amber { background: linear-gradient(135deg, #ff7e5f 0%, #feb47b 100%); }
.card-blue { background: linear-gradient(135deg, #2b5876 0%, #4e4376 100%); }
.card-emerald { background: linear-gradient(135deg, #1d976c 0%, #93f9b9 100%); color: #1a4731; }

.tips-banner { background: #e6f7ff; border: 1px solid #91d5ff; border-radius: 8px; padding: 12px 18px; font-size: 13.5px; color: #0050b3; display: flex; align-items: center; gap: 10px; margin-bottom: 20px; }
.content-block { background: #ffffff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 4px 18px rgba(0, 0, 0, 0.04); border: 1px solid #e2e8f0; }
.block-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; padding-bottom: 10px; border-bottom: 1px solid #edf2f7; }
.block-header .title { font-size: 16px; font-weight: bold; color: #1a202c; display: flex; align-items: center; gap: 6px; }
.block-header .desc { font-size: 12.5px; color: #a0aec0; }

/* 任务卡片 */
.task-card { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 10px; padding: 18px 20px; transition: all 0.2s ease; }
.task-card:hover { background: #ffffff; border-color: #cbd5e1; box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06); }
.task-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.task-card-top .t-name { font-size: 16px; font-weight: bold; color: #1e293b; }
.max-score-badge { background: #fee2e2; color: #ef4444; font-size: 12px; padding: 2px 10px; border-radius: 12px; font-weight: bold; }
.t-body p { font-size: 13.5px; color: #475569; margin: 6px 0; line-height: 1.5; }
.t-time { font-size: 12.5px; color: #94a3b8; display: flex; align-items: center; gap: 6px; margin-top: 6px; }
.task-card-footer { margin-top: 14px; display: flex; align-items: center; gap: 15px; }
.file-tag { font-size: 12.5px; color: #64748b; }
.file-tag span { color: #2563eb; font-weight: bold; }

/* 表格与徽章 */
.custom-table { border-radius: 8px; overflow: hidden; }
.badge { padding: 3px 10px; border-radius: 12px; font-size: 12px; font-weight: bold; }
.badge-gray { background: #f1f5f9; color: #64748b; }
.badge-yellow { background: #fef3c7; color: #d97706; }
.badge-green { background: #dcfce7; color: #15803d; }
.score-highlight { font-size: 15.5px; font-weight: bold; color: #16a34a; }

/* 教师端工具行 */
.teacher-box { padding: 0; border: none; background: transparent; box-shadow: none; }
.modern-tabs { border-radius: 12px; overflow: hidden; box-shadow: 0 4px 18px rgba(0, 0, 0, 0.05); }
.tab-title { font-weight: bold; display: flex; align-items: center; gap: 6px; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; background: #f8fafc; padding: 12px 18px; border-radius: 8px; border: 1px solid #e2e8f0; }
.label-text { font-size: 14px; font-weight: bold; color: #334155; }
.bar-left, .bar-right { display: flex; align-items: center; gap: 10px; }

/* 快捷评语 */
.quick-tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tag-click { cursor: pointer; transition: transform 0.15s; }
.tag-click:hover { transform: scale(1.05); }

/* 页脚 */
.app-footer { text-align: center; padding: 24px 0 35px 0; color: #94a3b8; font-size: 12.5px; line-height: 1.8; }
.app-footer p { margin: 0; }
.tech-stack { font-size: 11.5px; opacity: 0.8; }
</style>