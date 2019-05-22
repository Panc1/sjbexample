var ChannelRegister = function () {
    "use strict";
    return {
        /**
         * 接口指向地址
         */

        host: "http://reg.yryp6.com",
        randomStr: "",
        /**
         * 生成随机码
         * @param size
         * @returns {string}
         */
        randomCode: function (size) {
            // 备选数组
            var seed = new Array('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p', 'Q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '2', '3', '4', '5', '6', '7', '8', '9'
            );
            //数组长度
            var seedLength = seed.length;
            var createCode = '';
            for (var i = 0; i < size; i++) {
                var j = Math.floor(Math.random() * seedLength);
                createCode += seed[j];
            }
            return createCode;
        },
        /**
         * 加载图形验证码
         */
        loadImageCode: function () {
            this.randomStr = ChannelRegister.randomCode(36);
            $("#captchaImgBtn").attr("src", ChannelRegister.host + "/v2/borrowUser/captcha.svl?randomStr=" + this.randomStr);
        },
        /**
         * 是否函数
         * @param callFunction
         */
        isFunction: function (callFunction) {
            if (!(typeof callFunction === 'function')) {
                console.error("回调函数有误");
            }
        },
        /**
         * 用户是否存在
         * @param userPhone
         * @param callFunction
         */
        isExist: function (userPhone, callFunction) {

            $.ajax({
                type: "post",
                async: false,
                data: {userPhone: userPhone},
                url: this.host + "/v2/borrowUser/isExist",
                success: function (data) {
                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            });
        },
        /**
         * 发送短信验证码
         * @param userPhone
         * @param captcha
         * @param callFunction
         */
        sendSmsCode: function (userPhone, captcha, callFunction) {

            $.ajax({
                type: "post",
                async: false,
                data: {userPhone: userPhone, randomStr: ChannelRegister.randomStr, captcha: captcha},
                url: this.host + "/v2/borrowUser/sendSms",
                success: function (data) {

                    // 如果接口返回错误 那么重新加载图形验证码
                    if (data.code != '0') {
                        ChannelRegister.loadImageCode();
                    }
                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            })
        },
        /**
         * 发送短信验证码
         * @param userPhone
         * @param captcha
         * @param callFunction
         */
        sendSmsCode2: function (userPhone, callFunction) {

            $.ajax({
                type: "post",
                async: false,
                data: {userPhone: userPhone},
                url: this.host + "/v2/borrowUser/sendSms2",
                success: function (data) {
                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            })
        },
        register: function (userPhone, smsCode, channelCode, inviteCode, callFunction) {

            $.ajax({
                type: "post",
                async: false,
                data: {
                    userPhone: userPhone,
                    smsCode: smsCode,
                    channelCode: channelCode,
                    inviteCode: inviteCode
                },
                url: this.host + "/v2/borrowUser/channelRegister",
                success: function (data) {
                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            })
        },
        /**
         * 上传渠道信息
         * @param channelCode
         * @param callFunction
         */
        uploadChannelInfo: function (channelCode, callFunction) {
            if (!channelCode) {
                console.log("空的渠道码");
                return;
            }
            $.ajax({
                type: "post",
                async: false,
                data: {
                    uuid:localStorage.getItem("uuid")
                },
                url: this.host + "/v2/borrowUser/uploadChannelInfo/" + channelCode,
                success: function (data) {

                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            })
        },
        /**
         * 获取安卓和ios各自的下载地址
         * @param callFunction
         */
        getDownloadUrl: function (callFunction) {
            $.ajax({
                type: "post",
                async: false,
                data: {},
                url: this.host + "/v2/borrowUser/downUrl/",
                success: function (data) {
                    ChannelRegister.isFunction(callFunction);
                    console.log(data);
                    callFunction(data);
                }
            })
        },
        /**
         * 获取地址栏参数
         * @param name
         * @returns {*}
         */
        getQueryString: function (name) {
            var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
            var r = window.location.search.substr(1).match(reg);
            if (r != null) return unescape(r[2]);
            return null;
        },

        /**
         * 60s倒计时
         * @param element
         * @param count
         * @param noneElement
         * @param obj
         */
        RemainTime: function (element,obj,noneElement=null,count=60) {
            $(element).text(count);
            noneElement?$(noneElement).show():null;
            obj.flag = false;
            let timer = null;
            let startCountTime = (() => {
                timer = window.setInterval(() => {
                    if(count > 1){
                        count = count - 1;
                        $(element).text(count);
                    }else{
                        window.clearInterval(timer);
                        noneElement?$(noneElement).hide():null
                        $(element).text("点击重新获取");
                        obj.flag = true;
                    }
                },1000)
            })();
        },
        /**
         * 生成uuid
         */
        guid:function(){
            function S4() {
                return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
            }
            if(localStorage.getItem("time")){
                if(localStorage.getItem("time")!=new Date().toLocaleDateString()){
                    localStorage.setItem("uuid",S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
					localStorage.setItem("time",new Date().toLocaleDateString());
                }
            }else{
                localStorage.setItem("time",new Date().toLocaleDateString());
                localStorage.setItem("uuid",S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
            }
        }
    }
}();


$(function () {
    ChannelRegister.guid()
    // 记录渠道信息
    ChannelRegister.uploadChannelInfo(ChannelRegister.getQueryString("channelCode"), function (data) {
        // console.info(data);
    });

    $("#captchaImgBtn").on("click", ChannelRegister.loadImageCode);
    ChannelRegister.loadImageCode();
})



