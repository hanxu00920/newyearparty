$(function() {
    var speed = function() {
        return [0.3,0];
    };
    var canvas = document.createElement('canvas');
    canvas.id = 'myCanvas';
    canvas.width = document.body.offsetWidth;
    canvas.height = document.body.offsetHeight;
    document.getElementById('main').appendChild(canvas);
    
    new Vue({
        el: '#tools',
        data: {
            running: true,
            couponList: [{
                name: '特等奖'
            },
            {
                name: '一等奖'
            },
            {
                name: '二等奖'
            },
            {
                name: '三等奖'
            },
            {
                name: '四等奖'
            },
            {
                name: '抽红包'
            },
            ],
            pnum: null
        },
        created() {
            this.couponSelected = this.couponList[0].name;
        },
        mounted() {
        	TagCanvas.Start('myCanvas', '', {
                textColour: null,
                initial: speed(),
                noTagsMessage: false,
                shape: 'sphere',
                imageMode: "both",
                imagePosition: "top",
                textHeight: 10,
                noMouse: true,
                noSelect: true,
                maxSpeed: 0.01,
                radiusX: 1,
                radiusY: 1,
                radiusZ: 1
            });
        	setTimeout(this.getUser, 3000);
            setInterval(this.getUser,20000);
        },
        methods: {
            getUser: function() {
            	console.log(this.running)
            	if(this.running) {
            		var html = ['<ul>'];
                    $.ajax({
                        type: 'POST',
                        url: '/main/getLoginUser',
                        contentType: 'application/json',
                        success: function(response) {
                            response.userDatas.forEach(function(item, index) {
                                item.index = index;
                                html.push('<li><a href="#" style="color:white;"><img src="' + item.headimgurl + '" height="48px" width="48px">' + item.nickname + '(' + item.name + ')</a></li>');
                            });
                            html.push('</ul>');
                            canvas.innerHTML = html;
                            TagCanvas.Update('myCanvas');
                        },
                        error: function(xhr, type) {
                            alert('created() Ajax error!');
                            return '';
                        }
                    });
            	}
            },
            toggle: function() {
            	if (this.pnum == null) {
            		alert("请输入人数！");
            		return;
            	}
                if (this.running) {
                	if (confirm("请确认抽奖信息：人数【" + this.pnum + "】,奖等[" + this.couponSelected + "]")) {
                        TagCanvas.SetSpeed('myCanvas', [7, 2]);
                        this.running = !this.running;
                	}
                } else {
                	this.running = !this.running;
                	var html = "";
                    $.ajax({
                        type: 'POST',
                        data: JSON.stringify({
                            pnum: this.pnum,
                            lvl: this.couponSelected
                        }),
                        url: '/main/gameStart',
                        contentType: 'application/json',
                        success: function(response) { 
                        	if(response.retCode != "0000") {
                                alert("调用后台抽奖服务失败！["+response.retMsg+"]");
                        	} else {
                        		response.userDatas.forEach(function(item, index) {
                                    html = html + '<span><img src="' + item.headimgurl + '" height="48px" width="48px"><br/>' + item.nickname + '(' + item.name + ')</span>';
                                });
                        		setTimeout(function() {
                                    $('#main').addClass('mask');
                                },300);
                                $('#result').css('display', 'block').html(html + '<span><button width="50" id="closeResult">关闭本次抽奖结果</button></span>');
                                $('#closeResult').on('click', function(e){
                                	$('#result').css('display', 'none'); 
                                    $('#main').removeClass('mask');
                                });
                        	}
                            TagCanvas.SetSpeed('myCanvas', speed());
                        },
                        error: function(xhr, type) {
                            alert('Ajax error!');
                        }
                    });
                }
            }
        }
    });
});