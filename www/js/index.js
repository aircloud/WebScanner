
/**
 * Created by Xiaotao.Nie on 21/11/2016.
 * All right reserved
 * IF you have any question please email onlythen@yeah.net
 */

(function(){
    //一些预定义的变量
    var mytype=1;//服务类别，0代表端口扫描，1代表目录爆破
    var bginport=0;//端口扫描的起始端口
    var endport=65535;//端口扫描的结束端口
    var filetype=1;//目录爆破的时候的选择哪个文件，目前不支持用户自己上传文件，我们会尽量把文件弄的比较全
    var ifSelectAllport = 0;//端口扫描的时候是否选择了所有的端口

    var ifselectQuickPort=0;//是否快速扫描所有常用端口

    var canBegin = false;

    var selectFile = document.getElementById('select-file');

    var resultNumber = 2;

    var ipAddress = document.getElementById('ipaddress');

    var wsuri = "ws://localhost:8080/serv/chatendpoint";
    var ws = null;

    var type1 = document.getElementById('type1');
    var type2 = document.getElementById('type2');

    var type1More = document.getElementById('type1-more');
    var type2More = document.getElementById('type2-more');

    var lock = 0;


    var beginMode = document.getElementById('begin');

    var beginInput = document.getElementById('begin_input');
    var endInput = document.getElementById('end_input');

    var selectAllPort = document.getElementById('select_all_port');

    var selectQuickPort = document.getElementById('select_quick_port');

    var resultDiv = document.getElementById('result-div');

    var processExpand = document.getElementById("processExpand");

    type1.addEventListener('click',function (event) {
        mytype=1;
        type1More.classList.remove('not-display');
        !type2More.classList.contains('not-display'),type2More.classList.add('not-display');
        if(!type1.classList.contains('selected-option')) {
            type2.classList.contains('selected-option'),type2.classList.remove('selected-option');
            type1.classList.add('selected-option');
        }
    });
    type2.addEventListener('click',function(event){
        mytype=2;
        type2More.classList.remove('not-display');
        !type1More.classList.contains('not-display'),type1More.classList.add('not-display');
        if(!type2.classList.contains('selected-option')){
            if(type1.classList.contains('selected-option')){
                type1.classList.remove('selected-option');
            }
            type2.classList.add('selected-option');
        }
    });

    selectAllPort.addEventListener('click',function(event){
        if(!selectAllPort.classList.contains('highlight_text')){
            ifSelectAllport=1;ifselectQuickPort=0;
            selectAllPort.classList.add('highlight_text');
            selectQuickPort.classList.remove('highlight_text');
            beginInput.setAttribute('disabled','disabled');beginInput.classList.add("disabledInput");
            endInput.setAttribute('disabled','disabled');endInput.classList.add("disabledInput");
        }
        else {
            ifSelectAllport=0;
            selectAllPort.classList.remove('highlight_text');
            beginInput.removeAttribute('disabled');beginInput.classList.remove('disabledInput');
            endInput.removeAttribute('disabled');endInput.classList.remove('disabledInput');
        }
    });

    selectQuickPort.addEventListener('click',function(event){
        if(!selectQuickPort.classList.contains('highlight_text')){
            ifselectQuickPort=1;ifSelectAllport=0;
            selectQuickPort.classList.add('highlight_text');
            selectAllPort.classList.remove('highlight_text');
            beginInput.setAttribute('disabled','disabled');beginInput.classList.add("disabledInput");
            endInput.setAttribute('disabled','disabled');endInput.classList.add("disabledInput");
        }
        else {
            ifselectQuickPort=0;
            selectQuickPort.classList.remove('highlight_text');
            beginInput.removeAttribute('disabled');beginInput.classList.remove('disabledInput');
            endInput.removeAttribute('disabled');endInput.classList.remove('disabledInput');
        }
    });

    selectFile.addEventListener('change',function(event){
        filetype = selectFile.value;
    });

    function connectEndpoint(){
        window.WebSocket = window.WebSocket || window.MozWebSocket;
        if (!window.WebSocket){
            alert("WebSocket not supported by this browser");
            return;
        }

        ws = new WebSocket(wsuri);
        ws.onmessage = function(evt) {
            //alert(evt.data);
            // var old = document.getElementById("echo").value;
            // document.getElementById("echo").value = old+evt.data+"\r\n";
            var tempP = document.createElement('p');

            console.log(evt.data);

            if(evt.data) {

                if (evt.data == "扫描结束") {
                    beginMode.innerHTML = "BEGIN";
                    canBegin = false;
                    processExpand.style.width = "1000px";
                }


                if (evt.data.split("|").length >= 2) {

                    var tempArray = evt.data.split("|");

                    tempP.innerHTML = tempArray[2];
                    if(tempArray[2]) {
                        resultDiv.appendChild(tempP);
                        resultNumber++;
                    }
                    processExpand.style.width = (parseFloat(tempArray[1]) * 1000).toString() + "px";
                }

                else {

                    tempP.innerHTML = evt.data;
                    resultNumber++;
                    resultDiv.appendChild(tempP);

                }
                if (resultNumber > 15) {
                    resultDiv.style.overflow = "scroll";
                }
            }


        };

        ws.onclose = function(evt) {
            //alert("close");
            // document.getElementById("echo").value = "server disconnect.\r\n";
        };

        ws.onopen = function(evt) {
            //alert("open");
            // document.getElementById("echo").value = "connect server.\r\n";
        };
    }

    connectEndpoint();

    beginMode.addEventListener('click',function (event) {

        console.log(beginMode.childNodes.item(0).nodeValue,"nodevalue");
        if(beginMode.childNodes.item(0).nodeValue=="BEGIN") {
            var valueList = [],valueSend;
            /*
             * 十分有必要先统一一下valuelist的结构，
             * 结构为 端口扫描的情况： serviceType,ip地址，起始端口，结束端口,是否快速扫描常用端口
             * 或者   目录爆破的情况： serviceType,ip 地址或者网址，选择的文件的序号
             * */
            valueList.push(mytype);

            if(mytype==1){
                //端口扫描
                if (ipAddress.value && beginInput.value && endInput.value) {
                    valueList.push(ipAddress.value);
                    valueList.push(parseInt(beginInput.value));
                    valueList.push(parseInt(endInput.value));
                    valueList.push(ifselectQuickPort);
                    canBegin=true;
                    console.log("condition1");
                    beginMode.innerHTML = "STOP";

                }
                else if(ifSelectAllport && ipAddress.value){
                    valueList.push(ipAddress.value);
                    valueList.push(0);
                    valueList.push(65535);
                    valueList.push(ifselectQuickPort);
                    canBegin=true;
                    console.log("condition2");
                    beginMode.innerHTML = "STOP";
                }else if(ifselectQuickPort && ipAddress.value){
                    valueList.push(ipAddress.value);
                    valueList.push(0);
                    valueList.push(65535);
                    valueList.push(ifselectQuickPort);
                    canBegin=true;
                    console.log("condition3");
                    beginMode.innerHTML = "STOP";
                }
                else{
                    alert('请完善相关信息');
                }

            }
            else {
                //目录爆破
                if(ipAddress.value) {
                    valueList.push(ipAddress.value);
                    valueList.push(filetype);
                    canBegin = true;
                    beginMode.innerHTML = "STOP";
                }
                else{
                    alert('请完善相关信息');
                }
            }
            console.log(valueList);
            valueSend=valueList.join();
            if(canBegin) {
                ws.send(valueSend);
            }

        }
        else{
            beginMode.innerHTML="BEGIN";
            // ws.send('stop');
            ws=null;
            connectEndpoint();
            canBegin=false;
        }
        //to do ...
    });



}());

