$(function() {

    var latitude;
    var longitude;
	var openid;
	var nickname;
	var headimgurl;
    
    var url = "/main/getJsSdkConfig" + location.search;
    $.get(url,
    function(response) {
        if (response.retCode != "0000") {
            weui.alert("调用后台服务失败！");
        } else {
            wx.config({
                debug: false,
                // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
                appId: response.appId,
                // 必填，公众号的唯一标识
                timestamp: response.timestamp,
                // 必填，生成签名的时间戳
                nonceStr: response.noncestr,
                // 必填，生成签名的随机串
                signature: response.signature,
                // 必填，签名
                jsApiList: ['getLocation','hideMenuItems','updateAppMessageShareData']// 必填，需要使用的JS接口列表
            });
            wx.ready(function() {
            	wx.getLocation({
                    type: 'wgs84',
                    // 默认为wgs84的gps坐标，如果要返回直接给openLocation用的火星坐标，可传入'gcj02'
                    success: function(res) {
                        latitude = res.latitude; // 纬度，浮点数，范围为90 ~ -90
                        longitude = res.longitude; // 经度，浮点数，范围为180 ~ -180。
                        wx.updateAppMessageShareData({ 
        	        	    title: '易联达2020年会签到链接', // 分享标题
        	        	    desc: '分享给因为工作无法到现场的你', // 分享描述
        	        	    link: 'http://testurl',
        	        	    imgUrl: 'http://testurl/test.png', // 分享图标
        	        	    success: function () {
        	        	    	console.log("updateAppMessageShareData 设置成功");
        	        	    }
        	        	});
                    },
                    fail: function(res) {
                    	var obj = weui.alert("请开启定位服务后再进行签到！",function(){ wx.closeWindow();});
                    	setTimeout(function(){ obj.hide(); }, 3000);
                    }
                });
            	openid = response.openid;
            	nickname = response.nickname;
            	headimgurl = response.headimgurl;
            	wx.hideMenuItems({
        		  menuList: ['menuItem:editTag','menuItem:delete','menuItem:copyUrl','menuItem:originPage','menuItem:readMode','menuItem:openWithQQBrowser','menuItem:openWithSafari','menuItem:share:email','menuItem:share:timeline','menuItem:share:qq','menuItem:share:weiboApp','menuItem:favorite','menuItem:share:facebook','menuItem:share:QZone'] // 要隐藏的菜单项，只能隐藏“传播类”和“保护类”按钮，所有menu项见附录3
        		});
            });
        }
    });
    

    $('#haha').on('click',
    function() {
    	var alertDom = weui.alert('你咋这么顽皮？', {
    	    buttons: [{
    	        label: '三秒后自动关闭',
    	        type: 'primary',
    	        onClick: function(){ alert('皮！都说自动关了还点！'); }
    	    }]
    	});
    	setTimeout(function(){ alertDom.hide(); }, 3000);
    });

    var $s_name = $('#s_name');
    var $s_idlast = $('#s_idlast');

    $s_name.on('input',
    function() {
        if ($s_name.val() && $s_idlast.val()) {
            $('#b_submit').removeClass('weui-btn_disabled');
        } else {
            $('#b_submit').addClass('weui-btn_disabled');
        }
    });

    $s_idlast.on('input',
    function() {
        if ($s_name.val() && $s_idlast.val()) {
            $('#b_submit').removeClass('weui-btn_disabled');
        } else {
            $('#b_submit').addClass('weui-btn_disabled');
        }
    });

    $('#b_submit').on('click',
    function() {
        if ($(this).hasClass('weui-btn_disabled')) return;
        if (openid == null || openid == "") {
        	var obj = weui.alert("请使用微信扫码登录！");
        	setTimeout(function(){ obj.hide(); }, 3000);
        	return;
        }
        if (latitude == null || latitude == "") {
        	var obj = weui.alert("请开启定位服务后重新扫码签到！");
        	setTimeout(function(){ obj.hide(); }, 3000);
        	return;
        }
        if (longitude == null || longitude == "") {
        	var obj = weui.alert("请开启定位服务后重新扫码签到！");
        	setTimeout(function(){ obj.hide(); }, 3000);
        	return;
        }
        $.ajax({
            type: 'POST',
            url: '/main/login',
            data: JSON.stringify({
                name: $s_name.val(),
                idLast: $s_idlast.val(),
                longitude: longitude,
                latitude: latitude,
            	openid : openid,
	        	nickname : nickname,
	        	headimgurl : headimgurl,
            }),
            contentType: 'application/json',
            success: function(response) {
            	if(response.retCode == "0000") {
            		var obj = weui.alert(response.retMsg,function(){ wx.closeWindow();});
                	setTimeout(function(){ obj.hide(); }, 3000);
            	} else {
            		var obj = weui.alert(response.retMsg);
                	setTimeout(function(){ obj.hide(); }, 3000);
            	}
            },
            error: function(xhr, type) {
                var obj = weui.alert('Ajax[main/login] error!')
                setTimeout(function(){ obj.hide(); }, 3000);
            }
        });
    });

});