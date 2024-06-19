<script setup>
import {reactive} from 'vue';
import { User,Lock,Message,ChatLineSquare } from "@element-plus/icons-vue";
import router from "@/router/index.js";

const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: '',
})

const rule = {

}
</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">注册</div>
      <div style="font-size: 14px;color: grey">欢迎注册我们的网站，请在下方填写相关信息</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rule">
        <el-form-item>
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="密码">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.email" maxlength="20" type="text" placeholder="邮箱地址">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-row :gutter="10">
            <el-col :span="18">
                <el-input v-model="form.code" maxlength="6" type="text" placeholder="验证码">
                  <template #prefix>
                    <el-icon><ChatLineSquare /></el-icon>
                  </template>
                </el-input>
            </el-col>
            <el-col :span="6">
              <el-button type="success" plain>获取验证码</el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
    </div>
    <div style="margin-top: 50px;">
      <el-button style="width: 270px" type="warning" plain>立即注册</el-button>
    </div>
    <div style="margin-top: 30px">
        <span style="font-size: 15px;color: gray">已有账号？</span>
        <el-link @click="router.push('/')" style="transform: translateY(-2px);">立即登录</el-link>
      </div>
  </div>
</template>

<style scoped>

</style>