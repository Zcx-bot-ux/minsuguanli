<template>
    <div class="addEdit-block">
        <el-form
                class="detail-form-content"
                ref="ruleForm"
                :model="ruleForm"
                :rules="rules"
                label-width="100px"
                :style="{backgroundColor:addEditForm.addEditBoxColor}">
            <el-row>
                <!-- 房间信息 - 只读 -->
                <el-col :span="12">
                    <el-form-item class="input" label="房间名称" prop="fangjianName">
                        <el-input v-model="ruleForm.fangjianName" placeholder="房间名称" readonly></el-input>
                    </el-form-item>
                </el-col>

                <!-- 房间图片 - 详情展示 -->
                <el-col :span="12" v-if="type=='info' && ruleForm.fangjianPhoto">
                    <el-form-item label="房间图片">
                        <img :src="ruleForm.fangjianPhoto" style="width: 100px; height: 100px; border-radius: 4px;">
                    </el-form-item>
                </el-col>

                <!-- 用户信息 - 只读 -->
                <el-col :span="12">
                    <el-form-item class="input" label="用户姓名" prop="yonghuName">
                        <el-input v-model="ruleForm.yonghuName" placeholder="用户姓名" readonly></el-input>
                    </el-form-item>
                </el-col>

                <!-- 用户手机号 - 详情展示 -->
                <el-col :span="12" v-if="type=='info' && ruleForm.yonghuPhone">
                    <el-form-item label="用户手机号">
                        <span>{{ruleForm.yonghuPhone}}</span>
                    </el-form-item>
                </el-col>

                <!-- 评论/投诉内容 - 只读 -->
                <el-col :span="24">
                    <el-form-item label="评论/投诉内容" prop="fangjianLiuyanText">
                        <el-input type="textarea" v-model="ruleForm.fangjianLiuyanText" placeholder="评论/投诉内容" readonly :rows="4"></el-input>
                    </el-form-item>
                </el-col>

                <!-- 评论时间 - 详情展示 -->
                <el-col :span="12" v-if="type=='info' && ruleForm.insertTime">
                    <el-form-item label="评论时间">
                        <span>{{ruleForm.insertTime}}</span>
                    </el-form-item>
                </el-col>

                <!-- 回复内容 - 管理员可编辑 -->
                <el-col :span="24">
                    <el-form-item label="回复内容" prop="replyText">
                        <el-input type="textarea" v-model="ruleForm.replyText" placeholder="请输入回复内容" :readonly="type=='info'" :rows="4"></el-input>
                    </el-form-item>
                </el-col>

                <!-- 回复时间 - 详情展示 -->
                <el-col :span="12" v-if="type=='info' && ruleForm.updateTime">
                    <el-form-item label="回复时间">
                        <span>{{ruleForm.updateTime}}</span>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-form-item class="btn">
                <el-button v-if="type!='info'" type="primary" class="btn-success" @click="onSubmit">提交</el-button>
                <el-button v-if="type!='info'" class="btn-close" @click="back()">取消</el-button>
                <el-button v-if="type=='info'" class="btn-close" @click="back()">返回</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>
<script>
    import styleJs from "../../../utils/style.js";
    export default {
        data() {
            return {
                addEditForm:null,
                id: '',
                type: '',
                sessionTable : "",
                role : "",
                userId:"",
                fangjianForm: {},
                yonghuForm: {},
                ro:{
                    fangjianId: false,
                    yonghuId: false,
                    fangjianLiuyanText: false,
                    replyText: false,
                    insertTime: false,
                    updateTime: false,
                },
                ruleForm: {
                    fangjianId: '',
                    fangjianName: '',
                    fangjianPhoto: '',
                    yonghuId: '',
                    yonghuName: '',
                    yonghuPhone: '',
                    fangjianLiuyanText: '',
                    replyText: '',
                    insertTime: '',
                    updateTime: '',
                },
                fangjianOptions : [],
                yonghuOptions : [],
                rules: {
                   replyText: [
                          { required: true, message: '回复内容不能为空', trigger: 'blur' },
                      ],
                }
            };
        },
        props: ["parent"],
        created() {
            this.sessionTable = this.$storage.get("sessionTable");
            this.role = this.$storage.get("role");
            this.userId = this.$storage.get("userId");

            this.addEditForm = styleJs.addStyle();
            this.addEditStyleChange()
            this.addEditUploadStyleChange()
        },
        mounted() {
        },
        methods: {
            download(file){
                window.open(`${file}`)
            },
            init(id,type) {
                if (id) {
                    this.id = id;
                    this.type = type;
                }
                if(this.type=='info'||this.type=='else'){
                    this.info(id);
                }
            },
            // 多级联动参数
            info(id) {
                let _this =this;
                _this.$http({
                    url: `fangjianLiuyan/info/${id}`,
                    method: 'get'
                }).then(({ data }) => {
                    if (data && data.code === 0) {
                        _this.ruleForm = data.data;
                    } else {
                        _this.$message.error(data.msg);
                    }
                });
            },
            // 提交
            onSubmit() {
                this.$refs["ruleForm"].validate(valid => {
                    if (valid) {
                        // 只提交回复内容，其他字段保持原值
                        let submitData = {
                            id: this.ruleForm.id,
                            replyText: this.ruleForm.replyText
                        };
                        this.$http({
                            url:`fangjianLiuyan/update`,
                            method: "post",
                            data: submitData
                        }).then(({ data }) => {
                            if (data && data.code === 0) {
                                this.$message({
                                    message: "回复成功",
                                    type: "success",
                                    duration: 1500,
                                    onClose: () => {
                                        this.parent.showFlag = true;
                                        this.parent.addOrUpdateFlag = false;
                                        this.parent.fangjianLiuyanCrossAddOrUpdateFlag = false;
                                        this.parent.search();
                                        this.parent.contentStyleChange();
                                    }
                                });
                            } else {
                                this.$message.error(data.msg);
                            }
                        });
                    }
                });
            },
            getUUID () {
                return new Date().getTime();
            },
            back() {
                this.parent.showFlag = true;
                this.parent.addOrUpdateFlag = false;
                this.parent.fangjianLiuyanCrossAddOrUpdateFlag = false;
                this.parent.contentStyleChange();
            },

            addEditStyleChange() {
                this.$nextTick(()=>{
                    document.querySelectorAll('.addEdit-block .input .el-input__inner').forEach(el=>{
                        el.style.height = this.addEditForm.inputHeight
                        el.style.color = this.addEditForm.inputFontColor
                        el.style.fontSize = this.addEditForm.inputFontSize
                        el.style.borderWidth = this.addEditForm.inputBorderWidth
                        el.style.borderWidth = this.addEditForm.inputBorderStyle
                        el.style.borderColor = this.addEditForm.inputBorderColor
                        el.style.borderRadius = this.addEditForm.inputBorderRadius
                        el.style.backgroundColor = this.addEditForm.inputBgColor
                    })
                    document.querySelectorAll('.addEdit-block .input .el-form-item__label').forEach(el=>{
                        el.style.lineHeight = this.addEditForm.inputHeight
                        el.style.color = this.addEditForm.inputLableColor
                        el.style.fontSize = this.addEditForm.inputLableFontSize
                    })
                    document.querySelectorAll('.addEdit-block .textarea .el-textarea__inner').forEach(el=>{
                        el.style.color = this.addEditForm.textareaFontColor
                        el.style.fontSize = this.addEditForm.textareaFontSize
                        el.style.borderWidth = this.addEditForm.textareaBorderWidth
                        el.style.borderStyle = this.addEditForm.textareaBorderStyle
                        el.style.borderColor = this.addEditForm.textareaBorderColor
                        el.style.borderRadius = this.addEditForm.textareaBorderRadius
                        el.style.backgroundColor = this.addEditForm.textareaBgColor
                    })
                    document.querySelectorAll('.addEdit-block .btn .btn-success').forEach(el=>{
                        el.style.width = this.addEditForm.btnSaveWidth
                        el.style.height = this.addEditForm.btnSaveHeight
                        el.style.color = this.addEditForm.btnSaveFontColor
                        el.style.fontSize = this.addEditForm.btnSaveFontSize
                        el.style.borderWidth = this.addEditForm.btnSaveBorderWidth
                        el.style.borderStyle = this.addEditForm.btnSaveBorderStyle
                        el.style.borderColor = this.addEditForm.btnSaveBorderColor
                        el.style.borderRadius = this.addEditForm.btnSaveBorderRadius
                        el.style.backgroundColor = this.addEditForm.btnSaveBgColor
                    })
                    document.querySelectorAll('.addEdit-block .btn .btn-close').forEach(el=>{
                        el.style.width = this.addEditForm.btnCancelWidth
                        el.style.height = this.addEditForm.btnCancelHeight
                        el.style.color = this.addEditForm.btnCancelFontColor
                        el.style.fontSize = this.addEditForm.btnCancelFontSize
                        el.style.borderWidth = this.addEditForm.btnCancelBorderWidth
                        el.style.borderStyle = this.addEditForm.btnCancelBorderStyle
                        el.style.borderColor = this.addEditForm.btnCancelBorderColor
                        el.style.borderRadius = this.addEditForm.btnCancelBorderRadius
                        el.style.backgroundColor = this.addEditForm.btnCancelBgColor
                    })
                })
            },
            addEditUploadStyleChange() {
            },
        }
    };
</script>
<style lang="scss">
.editor{
  height: 500px;
  & ::v-deep .ql-container {
	  height: 310px;
  }
}
.amap-wrapper {
  width: 100%;
  height: 500px;
}
.search-box {
  position: absolute;
}
.addEdit-block {
	margin: -10px;
}
.detail-form-content {
	padding: 12px;
}
.btn .el-button {
  padding: 0;
}
</style>

