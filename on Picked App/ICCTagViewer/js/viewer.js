"use strict";

String.prototype.format = function(args) {
    let reg;
    if (arguments.length > 0) {
        let result = this;
        if (arguments.length === 1 && typeof(args) == "object") {
            for (const key in args) {
                if (!args.hasOwnProperty(key)) continue;
                reg = new RegExp("({" + key + "})", "g");
                result = result.replace(reg, args[key]);
            }
        } else {
            for (let i = 0; i < arguments.length; i++) {
                if (arguments[i] === undefined) {
                    return "";
                } else {
                    reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
        return result;
    } else {
        return this;
    }
};

const HTMLDecode = function(text) {
    let temp = document.createElement("div");
    temp.innerHTML = text;
    const output = temp.innerText || temp.textContent;
    temp = null;
    return output;
};

const ICCTAGViewer = {
    tagsCnt: 0,
    $copySrc: null,
    data: {
        tags: [
            {
                "id": "entryMethod",
                "name": "入口方法",
                "subTags": [
                    {"id": "isLifeCycle", "name": "生命周期方法"},
                    {"id": "isStaticCallBack", "name": "静态回调方法"},
                    {"id": "isDynamicCallBack", "name": "动态回调方法"},
                    {"id": "isImplicitCallback", "name": "隐式回调方法"}
                ],
            },
            {
                "id": "exitMethod",
                "name": "出口方法",
                "subTags": [
                    {"id": "isNormalSendICC", "name": "普通 ICC"},
                    // {"id": "isWrapperSendICC", "name": "Wrapper ICC"},
                    {"id": "isWarpperSendICC", "name": "Wrapper ICC"},
                ],
            },
            {
                "id": "intentMatch",
                "name": "Intent 模式",
                "subTags": [
                    {"id": "isExplicit", "name": "显式 Intent"},
                    {"id": "isImplicit", "name": "隐式 Intent"},
                ],
            },
            {
                "id": "analyzeScope",
                "name": "分析域",
                "subTags": [
                    {
                        "id": "componentScope", "name": "组件类型", "subTags": [
                            {"id": "isActivity", "name": "Activity"},
                            {"id": "isService", "name": "Service"},
                            {"id": "isBroadCast", "name": "Broadcast"},
                            {"id": "isDynamicBroadCast", "name": "DynamicBroadcast"},
                        ]
                    },
                    {
                        "id": "nonComponentScope", "name": "非组件类型", "subTags": [
                            {"id": "isFragment", "name": "Fragment"},
                            {"id": "isAdapter", "name": "Adapter"},
                            {"id": "isWidget", "name": "Widget"},
                            {"id": "isOtherClass", "name": "其它自定义类"},
                        ]
                    },
                    {
                        "id": "methodScope", "name": "方法类型", "subTags": [
                            {"id": "isLibraryInvocation", "name": "库方法调用"},
                            {"id": "isBasicInvocation", "name": "简单调用"},
                            {"id": "isAsyncInvocation", "name": "异步调用"},
                            {"id": "isListenerInvocation", "name": "监听回调"},
                            {"id": "isPolymorphic", "name": "多态"}
                        ]
                    },
                    {
                        "id": "objectScope", "name": "对象类型", "subTags": [
                            {"id": "isStaticVal", "name": "静态值"},
                            {"id": "isStringOp", "name": "字符串操作"}
                        ]
                    },
                    {
                        "id": "sensitivityScope", "name": "敏感性", "subTags": [
                            /*
                            {"id": "isFlow", "name": "流敏感"},
                            {"id": "isPath", "name": "路径敏感"},
                            {"id": "isContext", "name": "上下文敏感"},
                            {"id": "isObject", "name": "对象敏感"},
                            {"id": "isField", "name": "域敏感"},
                             */
                            {"id": "flow", "name": "流敏感"},
                            {"id": "path", "name": "路径敏感"},
                            {"id": "context", "name": "上下文敏感"},
                            {"id": "object", "name": "对象敏感"},
                            {"id": "field", "name": "域敏感"},
                        ]
                    },
                ],
            },
        ],

        flows: [
            {"source": "com.abc.test1", "dest": "com.abc.test2", "method": "xxx"},
            {"source": "com.abc.test2", "dest": "com.abc.test3", "method": "xxx"},
        ],
    },

    saveLocal: function() {
        const xmlStr = this._exportXML();
        localStorage.setItem('icc-xml', xmlStr);
        this.makeToast('数据已保存到本地');
    },

    readLocal: function() {
        const xmlStr = localStorage.getItem('icc-xml');
        if (xmlStr) {
            console.log('Local storage xml detected');
            this._importXML(xmlStr);
            this.makeToast('已恢复本地保存的数据');
        } else {
            // this.makeToast('没有检测到本地数据', 3, 'bg-warning');
        }
    },

    clearLocal: function() {
        localStorage.removeItem('icc-xml');
        this.makeToast('已删除本地保存的数据');
    },

    initSingleTag: function(tag) {
        const _this = this;
        const $div = $('<div class="form-check form-check-inline" />')
        const $label = $('<label class="form-check-label icc-tag-label" type="button" />');
        const $checkBox = $('<input class="form-check-input icc-tag" type="checkbox" />');
        $checkBox.attr('value', tag.id);
        $label.attr('title', tag.id);
        $label.append($checkBox).append(tag.name);
        $div.append($label);
        return $div;
    },

    initAccordionItem: function(idPrefix, itemTitle, bsParent, extraClass = undefined) {
        const $itemDiv = $('<div class="accordion-item" />');
        $itemDiv.attr('id', idPrefix);
        if (extraClass) $itemDiv.addClass(extraClass);

        // Generate Heading
        const $heading = $('<h2 class="accordion-header" />');
        $heading.attr('id', '{0}-heading'.format(idPrefix));

        const $btn = $('<button class="accordion-button collapsed btn" type="button" />');
        $btn.attr('data-bs-toggle', 'collapse')
            .attr('data-bs-target', '#{0}-content'.format(idPrefix))
            .attr('aria-expanded', false)
            .attr('aria-controls', '{0}-content'.format(idPrefix));

        if (extraClass === 'icc-flow') itemTitle += ' /&nbsp;<b style="color: blue"><span class="tags-num" /></b>';
        $btn.html(itemTitle);
        $heading.append($btn);
        $itemDiv.append($heading);

        // Generate Content
        const $content = $('<div class="accordion-collapse collapse"/>');
        $content.attr('id', '{0}-content'.format(idPrefix))
            .attr('aria-labelledby', '{0}-heading'.format(idPrefix))
            .attr('data-bs-parent', '#{0}'.format(idPrefix))
            .attr('data-bs-parent-fake', '#{0}'.format(idPrefix))
            .attr('data-bs-parent-real', '#{0}'.format(bsParent));
        $itemDiv.append($content);
        return $itemDiv;
    },

    buildTagDiv: function(tag, idPrefix, $rootDiv) {
        const tagIdPrefix = '{0}-{1}'.format(idPrefix, tag.id);
        let $thisTagDiv;
        if (tag.hasOwnProperty('subTags')) {
            $thisTagDiv = this.initAccordionItem(tagIdPrefix, tag.name + ' (' + tag.id + ')', idPrefix);
            const $thisTagContentDiv = $thisTagDiv.find('.accordion-collapse');

            // Has Sub Tags, build an accordion
            const $subTagsDiv = $('<div class="accordion container p-3" />');
            $subTagsDiv.attr('id', tagIdPrefix);

            // Build all sub tags
            for (const i in tag.subTags) {
                if (!tag.subTags.hasOwnProperty(i)) continue;
                const subTag = tag.subTags[i];
                this.buildTagDiv(subTag, tagIdPrefix, $subTagsDiv);
            }
            $thisTagContentDiv.append($subTagsDiv);
        } else {
            // Has no Sub Tags, build label and input
            $thisTagDiv = this.initSingleTag(tag);
        }
        $rootDiv.append($thisTagDiv);
    },

    addCallLine: function($baseDiv, params, $relatedDiv = null) {
        const _this = this;
        const $lineDiv = $('<div class="row align-items-center call-line" />');
        const $lineCol1 = $('<div class="col-sm-2">');
        $lineDiv.append($lineCol1);

        const $lineCol2 = $('<div class="col-sm-6">');
        const $lineSrcInput = $('<input class="form-control call-line-src" type="text" value="src/main/com/test/abc/MainActivity.java" />');
        $lineSrcInput.val(params.src ? params.src : '');
        $lineSrcInput.on('input', function() {
            let path = $lineSrcInput.val();
            if (path[0] !== '/') path = '/' + path;
            $lineSrcInput.val(path.replaceAll('\\', '/'));
        });
        $lineCol2.append($lineSrcInput);
        $lineDiv.append($lineCol2);

        const $lineCol3 = $('<div class="col-sm-1">');
        const $lineCol4 = $('<div class="col-sm-1">');
        const $lineStartInput = $('<input class="form-control call-line-st" type="number" value="10" />');
        const $lineEndInput = $('<input class="form-control call-line-ed" type="number" value="11" />');
        $lineStartInput.val(params.start ? params.start : 1);
        $lineEndInput.val(params.end ? params.end : 1);
        $lineCol3.append($lineStartInput);
        $lineCol4.append($lineEndInput);
        $lineStartInput.on('blur', function() {
            // Auto-fill
            const $callLineDivs = $('.call-line');
            for (const i in $callLineDivs) {
                if (!$callLineDivs.hasOwnProperty(i)) continue;
                if ($callLineDivs[i] === $lineDiv[0]) continue;
                const $callLineDiv = $($callLineDivs[i]);
                if ($lineSrcInput.val() !== '' &&
                    $callLineDiv.find('.call-line-src').val() === $lineSrcInput.val() &&
                    $callLineDiv.find('.call-line-st').val() === $lineStartInput.val()) {
                    $lineEndInput.val($callLineDiv.find('.call-line-ed').val());
                    return true;
                }
            }
            const st = parseInt($lineStartInput.val());
            const ed = parseInt($lineEndInput.val());
            if (ed < st) $lineEndInput.val($lineStartInput.val());
        });
        $lineDiv.append($lineCol3);
        $lineDiv.append($lineCol4);

        const $lineCol5 = $('<div class="col-sm-1">');
        const $lineAddBtn = $('<button class="btn btn-sm btn-success ms-1" type="button"><i class="bi bi-plus-circle" /></button>');
        $lineAddBtn.on('click', function() {
            _this.addCallLine($baseDiv, {}, $lineDiv);
        });
        $lineCol5.append($lineAddBtn);
        $lineDiv.append($lineCol5);

        // const $lineCol6 = $('<div class="col-sm-1">');
        const $lineDelBtn = $('<button class="btn btn-sm btn-danger ms-1" type="button"><i class="bi bi-dash-circle" /></button>');
        $lineDelBtn.on('click', function() {
            $lineDiv.remove();
        });
        $lineCol5.append($lineDelBtn);

        if ($relatedDiv) {
            const $lineDivs = $baseDiv.find('.call-line');
            let i;
            for (i in $lineDivs) {
                if (!$lineDivs.hasOwnProperty(i)) continue;
                if ($lineDivs[i] === $relatedDiv[0]) break;
            }
            $lineDiv.insertBefore($($lineDivs[i]));
        } else {
            $baseDiv.append($lineDiv);
        }
        $lineSrcInput.trigger('focus');
    },

    XMLToCallLine: function($linesDiv, callLinesNode) {
        // if (callLinesNode.children.length < 1) return;
        $linesDiv.find('.call-line').remove();
        for (const i in callLinesNode.children) {
            if (!callLinesNode.children.hasOwnProperty(i)) continue;
            const lineNode = callLinesNode.children[i];
            this.addCallLine($linesDiv, {
                src: lineNode.getAttribute('src'),
                start: lineNode.getAttribute('start'),
                end: lineNode.getAttribute('end')
            });
        }
    },

    callLineToXML: function($linesDiv) {
        const xml = this.createXMLDOM();
        const callLinesNode = xml.createElement('callLines');
        const $lineDivs = $linesDiv.find('.call-line');
        $lineDivs.each(function(i, elem) {
            const $lineDiv = $(elem);
            const lineNode = xml.createElement('line');
            lineNode.setAttribute('src', $lineDiv.find('.call-line-src').val());
            lineNode.setAttribute('start', $lineDiv.find('.call-line-st').val());
            lineNode.setAttribute('end', $lineDiv.find('.call-line-ed').val());
            callLinesNode.append(lineNode);
        });
        return callLinesNode;
    },

    initTagSelector: function(idPrefix, flowId, flowObj) {
        const _this = this;
        const $container = $('<div class="container pb-3" />');

        const $basicInfo = $('<div class="basic-info mb-3"/>');
        const $ctrlDiv = $('<div class="row mb-3 align-items-center" />');

        // Controller
        // Copy Button
        const $copyBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 btn-copy">拷贝特性</button>');
        $copyBtn.on('click', function() {
            let $parent = $(this).parent();
            while (!$parent.hasClass('accordion-item')) $parent = $parent.parent();
            _this.$copySrc = $parent;
            $('.btn-paste').prop('disabled', false);
            _this.makeToast('拷贝成功', 2);
        });
        $ctrlDiv.append($copyBtn);

        // Paste Button
        const $pasteBtn = $('<button class="col-auto btn btn-sm btn-success ms-2 btn-paste" disabled>粘贴特性</button>');
        $pasteBtn.on('click', function() {
            if (!_this.$copySrc) {
                _this.makeToast('请先复制特性', 2, 'bg-warning', 'bi-exclamation-triangle');
            }
            let $parent = $(this).parent();
            while (!$parent.hasClass('accordion-item')) $parent = $parent.parent();
            const $copyTarget = $parent;
            const $sourceInputs = _this.$copySrc.find('.icc-tag');
            const $targetInputs = $copyTarget.find('.icc-tag');
            for (const i in $targetInputs) {
                if (!$targetInputs.hasOwnProperty(i)) continue;
                if (!$sourceInputs.hasOwnProperty(i)) continue;
                const $dest = $($targetInputs[i]);
                const $src = $($sourceInputs[i]);
                // console.log($dest, $src);
                $dest.prop('checked', $src.prop('checked'));
            }
            _this.countSelectedTags($('#icc-flow-{0}'.format(flowId)));
            _this.makeToast('粘贴成功', 2);
        });
        $ctrlDiv.append($pasteBtn);

        // Collapse Button
        const $collapseBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 j-collapse">展开所有</button>');
        $ctrlDiv.append($collapseBtn);

        // Edit & Cancel Button
        const $editBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 j-edit">编辑信息</button>');
        const $cancelBtn = $('<button class="col-auto btn btn-sm btn-danger ms-2 j-edit-cancel">取消更改</button>');
        $ctrlDiv.append($editBtn);
        $ctrlDiv.append($cancelBtn.hide());

        $editBtn.on('click', function() {
            if ($editBtn.hasClass('btn-primary')) {
                $container.find('.detail-show').hide();
                $container.find('.detail-edit').show();
                $editBtn.removeClass('btn-primary').addClass('btn-success').html('保存更改');
                $cancelBtn.show();
            } else {
                _this.editFlow(flowId, flowObj, $basicInfo);
                $container.find('.detail-show').show();
                $container.find('.detail-edit').hide();
                $editBtn.removeClass('btn-success').addClass('btn-primary').html('编辑信息');
                $cancelBtn.hide();
            }
        });
        $cancelBtn.on('click', function() {
            $container.find('.detail-show').show();
            $container.find('.detail-edit').hide();
            $editBtn.removeClass('btn-success').addClass('btn-primary').html('编辑信息');
            $cancelBtn.hide();
        });

        // Delete Button
        const $delBtn = $('<button class="col-auto btn btn-sm btn-danger ms-2 j-delete">删除该边</button>');
        $delBtn.on('click', function() {
            _this.delFlow(flowId);
        });
        $ctrlDiv.append($delBtn);
        $container.append($ctrlDiv);

        // Basic Information
        // Source
        const $sourceDiv = $('<div class="row align-items-center" />');
        const $sourceCol1 = $('<div class="col-sm-1">');
        const $sourceCol2 = $('<div class="col-sm-6 detail-edit">').hide();
        const $sourceCol3 = $('<div class="col-sm-6 detail-show">');
        $sourceCol1.html('<label class="col-form-label">源类名：</label>');
        const $sourceInput = $('<input class="form-control icc-source-edit" type="text" />').val(flowObj.source);
        $sourceCol2.append($sourceInput);
        const $sourceSpan = $('<span class="display-name icc-source-show" />').html(flowObj.source);
        $sourceCol3.append($sourceSpan);
        $sourceDiv.append($sourceCol1).append($sourceCol2).append($sourceCol3);
        $basicInfo.append($sourceDiv);

        // Target
        const $destDiv = $('<div class="row align-items-center" />');
        const $destCol1 = $('<div class="col-sm-1">');
        const $destCol2 = $('<div class="col-sm-6 detail-edit">').hide();
        const $destCol3 = $('<div class="col-sm-6 detail-show">');
        $destCol1.html('<label class="col-form-label">目标类名：</label>');
        const $destInput = $('<input class="form-control icc-dest-edit" type="text" />').val(flowObj.dest);
        $destCol2.append($destInput);
        const $destSpan = $('<span class="display-name icc-dest-show" />').html(flowObj.dest);
        $destCol3.append($destSpan);
        $destDiv.append($destCol1).append($destCol2).append($destCol3);
        $basicInfo.append($destDiv);

        // Method
        const $methodDiv = $('<div class="row align-items-center" />');
        const $methodCol1 = $('<div class="col-sm-1">');
        const $methodCol2 = $('<div class="col-sm-6 detail-edit">').hide();
        const $methodCol3 = $('<div class="col-sm-6 detail-show">');
        $methodCol1.html('<label class="col-form-label">方法名：</label>');
        const $methodInput = $('<input class="form-control icc-method-edit" type="text" />').val(flowObj.method);
        $methodCol2.append($methodInput);
        const $methodSpan = $('<span class="display-name icc-method-show" />').html(flowObj.method);
        $methodCol3.append($methodSpan);
        $methodDiv.append($methodCol1).append($methodCol2).append($methodCol3);
        $basicInfo.append($methodDiv);

        // Comment
        const $commentDiv = $('<div class="row align-items-center" />');
        const $commentCol1 = $('<div class="col-sm-1">');
        const $commentCol2 = $('<div class="col-sm-11 detail">');
        $commentCol1.html('<label class="col-form-label">备注：</label>');
        const $commentInput = $('<input class="form-control icc-comment-edit" type="text" />').val(flowObj.comment);
        $commentCol2.append($commentInput);
        $commentDiv.append($commentCol1).append($commentCol2);
        $basicInfo.append($commentDiv);

        // Lines
        const $linesDiv = $('<div class="call-lines" />');
        const $lineDiv = $('<div class="row align-items-center" />');
        const $lineFirstCol1 = $('<div class="col-sm-2">');
        const $lineFirstLabel = $('<label class="col-form-label">调用代码行：</label>');
        $lineFirstCol1.append($lineFirstLabel);
        const $lineAddBtn = $('<button class="btn btn-sm btn-success" type="button"><i class="bi bi-plus-circle" /></button>');
        $lineFirstCol1.append($lineAddBtn);
        $lineAddBtn.on('click', function() {
            _this.addCallLine($linesDiv, {});
        });
        $lineDiv.append($lineFirstCol1);
        $lineDiv.append($('<div class="col-sm-6 text-center">源文件</div>'));
        $lineDiv.append($('<div class="col-sm-1 text-center">起始行</div>'));
        $lineDiv.append($('<div class="col-sm-1 text-center">结束行</div>'));
        $linesDiv.append($lineDiv);
        this.addCallLine($linesDiv, {});
        $basicInfo.append($linesDiv);

        // Counter
        const $counterDiv = $('<div class="row align-items-center mt-2" />');
        const $counterCol = $('<div class="col-auto"></div>');
        const $counter = $('<strong>已选择特性数量：<span class="tags-num">0</span>/{0}</strong>'.format(this.tagsCnt));
        $counterCol.append($counter);
        $basicInfo.append($counterDiv.append($counterCol));

        $container.append($basicInfo);

        // Generate Selector
        const $div = $('<div class="accordion" />');
        $div.attr('id', idPrefix);
        for (const i in this.data.tags) {
            if (!this.data.tags.hasOwnProperty(i)) continue;
            const tag = this.data.tags[i];
            this.buildTagDiv(tag, idPrefix, $div);
        }
        $container.append($div);
        return $container;
    },

    getFlowName: function(flowObj) {
        const source = flowObj.source.split('.').pop();
        const dest = flowObj.dest.split('.').pop();
        const method = flowObj.method.split('.').pop();
        return '{0} --> {1} [{2}]'.format(source, dest, method);
    },

    initFlow: function(flowId, flowObj) {
        const idPrefix = 'icc-flow-{0}'.format(flowId);
        const flowName = this.getFlowName(flowObj);
        const $flowDiv = this.initAccordionItem(idPrefix, flowName, 'icc-flows', 'icc-flow');
        const $selectorDiv = this.initTagSelector(idPrefix, flowId, flowObj);
        $selectorDiv.addClass('accordion-body');
        $flowDiv.find('.accordion-collapse').append($selectorDiv);
        return $flowDiv;
    },

    initFlows: function() {
        const _this = this;
        const $flowsDiv = $('<div id="icc-flows" />');
        $flowsDiv.addClass('accordion');
        for (const i in this.data.flows) {
            if (!this.data.flows.hasOwnProperty(i)) continue;
            const flow = this.data.flows[i];
            const $flowDiv = this.initFlow(i, flow);

            $flowDiv.find('.icc-tag-label').each(function(i, elem) {
                $(elem).on('click', function() {
                    _this.countSelectedTags($flowDiv);
                });
            });

            if (flow.metaData) {
                for (const j in flow.metaData) {
                    if (!flow.metaData.hasOwnProperty(j)) continue;
                    const dom = flow.metaData[j];
                    if (dom.tagName === 'tags') {
                        this.initXMLTags(dom, $flowDiv);
                        this.countSelectedTags($flowDiv);
                    } else if (dom.tagName === 'callLines') {
                        const $linesDiv = $flowDiv.find('.call-lines');
                        this.XMLToCallLine($linesDiv, dom);
                    }
                }
            }
            $flowsDiv.append($flowDiv);
        }
        $('#icc-flows').remove();
        $('.container-main').append($flowsDiv);
        this.initCollapseBtn();
    },

    initCollapseBtn: function($scope = undefined) {
        let $collapse;
        if ($scope) {
            $collapse = $scope.find('.j-collapse');
        } else {
            $collapse = $('.j-collapse');
        }
        $collapse.off('click').on('click', function(e) {
            e.stopPropagation();
            e.preventDefault();
            const $btn = $(this);
            let $parent = $btn.parent();
            while (!$parent.hasClass('accordion-collapse') && !$parent.hasClass('container-main')) {
                $parent = $parent.parent();
            }
            // console.log('$parent', $parent);
            if (!$btn.hasClass('j-collapse-show')) {
                $parent.find('.accordion-collapse').collapse('show');
                $btn.addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html('折叠所有');
                $parent.find('.j-collapse').addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html('折叠所有');
            } else {
                $parent.find('.accordion-collapse').collapse('hide');
                $btn.removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html('展开所有');
                $parent.find('.j-collapse').removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html('展开所有');
            }
        })
    },

    countSelectedTags: function($flowDiv) {
        const cnt = $flowDiv.find('input:checked').length;
        $flowDiv.find('.tags-num').html(cnt);
    },
    
    countTags: function(tag) {
        if (!tag.hasOwnProperty('subTags')) this.tagsCnt++;
        else {
            for (const i in tag.subTags) {
                if (!tag.subTags.hasOwnProperty(i)) continue;
                const subTag = tag.subTags[i];
                this.countTags(subTag);
            }
        }
    },

    createXMLDOM: function() {
        let xmlDOM;
        if (window.ActiveXObject) {
            xmlDOM = new ActiveXObject('Microsoft.XMLDOM');
        } else if (document.implementation
            && document.implementation.createDocument) {
            xmlDOM = document.implementation.createDocument('', '', null);
        } else {
            alert('您的浏览器不支持文档对象XMLDOM');
            return;
        }
        return xmlDOM;
    },

    parserStringToXMLDOM: function(str) {
        const parser = new DOMParser();
        return parser.parseFromString(str, 'text/xml');
    },

    parserXMLToString: function(xmlDOM) {
        if (window.ActiveXObject) {
            return xmlDOM.xml;
        } else if (document.implementation
            && document.implementation.createDocument) {
            return new XMLSerializer().serializeToString(xmlDOM);
        }
    },

    exportTagsXML: function(tag, xmlDom, nameStack, rootNode) {
        if (tag.hasOwnProperty('subTags')) {
            const tagNode = xmlDom.createElement(tag.id);
            nameStack.push(tag.id);
            for (const i in tag.subTags) {
                if (!tag.subTags.hasOwnProperty(i)) continue;
                const subTag = tag.subTags[i];
                this.exportTagsXML(subTag, xmlDom, nameStack, tagNode);
            }
            nameStack.pop();
            rootNode.appendChild(tagNode);
        } else {
            const $contentDiv = $('#{0}-content'.format(nameStack.join('-')));
            const $checkBox = $contentDiv.find('input[value="{0}"]'.format(tag.id));
            const tagValue = $checkBox.prop('checked');
            rootNode.setAttribute(tag.id, tagValue);
        }
    },

    formatXML: function(xml, tab = '  ', nl = '\n') {
        let formatted = '', indent = '';
        const nodes = xml.slice(1, -1).split(/>\s*</);
        if (nodes[0][0] === '?') formatted += '<' + nodes.shift() + '>' + nl;
        for (let i = 0; i < nodes.length; i++) {
            const node = nodes[i];
            if (node[0] === '/') indent = indent.slice(tab.length);
            formatted += indent + '<' + node + '>' + nl;
            if (node[0] !== '/' && node[node.length - 1] !== '/' && node.indexOf('</') === -1) indent += tab;
        }
        return formatted;
    },

    _exportXML: function() {
        const xml = this.createXMLDOM();
        const rootNode = xml.createElement('root');
        xml.appendChild(rootNode);

        for (const i in this.data.flows) {
            if (!this.data.flows.hasOwnProperty(i)) continue;
            const flow = this.data.flows[i];
            if (flow.isDelete) {
                continue;
            }
            const flowDomId = 'icc-flow-{0}'.format(i);
            const $flowDiv = $('#{0}'.format(flowDomId));
            const edgeNode = xml.createElement('OracleEdge');
            edgeNode.setAttribute('source', flow.source);
            edgeNode.setAttribute('destination', flow.dest);
            edgeNode.setAttribute('method', flow.method);

            // Add metaData
            if (flow.hasOwnProperty('metaData')) {
                for (const j in flow.metaData) {
                    if (!flow.metaData.hasOwnProperty(j)) continue;
                    const dom = flow.metaData[j];
                    if (dom.tagName === 'tags' || dom.tagName === 'callLines') continue;
                    edgeNode.appendChild(dom);
                }
            }

            // Comment
            const commentNode = xml.createElement('comment');
            commentNode.innerHTML = $flowDiv.find('.icc-comment-edit').val();
            edgeNode.appendChild(commentNode);

            // Call Lines
            const $linesDiv = $flowDiv.find('.call-lines');
            const callLinesNode = this.callLineToXML($linesDiv);
            edgeNode.appendChild(callLinesNode);

            // Tags
            const tagsNode = xml.createElement('tags');
            for (const j in this.data.tags) {
                if (!this.data.tags.hasOwnProperty(j)) continue;
                const tag = this.data.tags[j];
                this.exportTagsXML(tag, xml, [flowDomId], tagsNode);
            }
            edgeNode.appendChild(tagsNode);

            rootNode.appendChild(edgeNode);
        }
        return this.parserXMLToString(xml);
    },

    exportXML: function() {
        let resStr = this.formatXML(this._exportXML());
        resStr = '<?xml version="1.0" encoding="UTF-8"?>\n\n' + resStr;
        $('#xmlCode').val(resStr);
        const $xmlModal = $('#xmlModal');
        const xmlModal = new bootstrap.Modal($xmlModal);
        $xmlModal.find('.modal-title-text').html('导出 XML');
        $xmlModal.find('.j-confirm').off('click').on('click', function() {
            xmlModal.hide();
        });
        xmlModal.toggle();
        return true;
    },

    initXMLTags: function(edgeElem, $flowDiv, nameStack = []) {
        if (edgeElem.tagName !== 'tags') nameStack.push(edgeElem.tagName);
        if (edgeElem.children.length > 0) {
            for (const i in edgeElem.children) {
                if (!edgeElem.children.hasOwnProperty(i)) continue;
                const node = edgeElem.children[i];
                this.initXMLTags(node, $flowDiv, nameStack);
            }
            if (edgeElem.tagName !== 'tags') nameStack.pop();
        } else {
            const domId = '{0}-{1}'.format($flowDiv.attr('id'), nameStack.join('-'));
            const $tagsDiv = $flowDiv.find('#{0}'.format(domId));
            for (const i in edgeElem.attributes) {
                if (!edgeElem.attributes.hasOwnProperty(i)) continue;
                const tagAttr = edgeElem.attributes[i];
                const checked = (tagAttr.nodeValue === 'true');
                const $input = $tagsDiv.find('input[value="{0}"]'.format(tagAttr.name));
                $input.prop('checked', checked);
                if ($input.length < 1) {
                    console.log('Warning: tag [{0}-{1}] not found!'.format(domId, tagAttr.name));
                }
            }
        }
        nameStack.pop();
    },

    _importXML: function(xmlStr) {
        const _this = this;
        const xml = this.parserStringToXMLDOM(xmlStr);

        if (xml.getElementsByTagName('parsererror').length > 0) {
            this.makeToast('XML 格式有误', 2,'bg-danger', 'bi-x-circle')
            return false;
        }
        const $optionOnlyOne = $('.option-onlyOne');
        if ($optionOnlyOne.prop('checked')) {
            $optionOnlyOne.trigger('click');
            window.setTimeout(function() {
                _this._importXML(xmlStr);
            }, 500);
            return true;
        }
        const edges = xml.getElementsByTagName('OracleEdge');

        this.data.flows.length = 0;
        for (const i in edges) {
            if (!edges.hasOwnProperty(i)) continue;
            const edge = edges[i];

            // Put into data
            const metaData = [];
            let commentElem = null;
            for (const j in edge.children) {
                if (!edge.children.hasOwnProperty(j)) continue;
                const child = edge.children[j];
                if (child.tagName === 'comment') {
                    commentElem = child;
                    continue;
                }
                metaData.push(child);
            }
            const flowObj = {
                source: edge.getAttribute('source'),
                dest: edge.getAttribute('destination'),
                method: edge.getAttribute('method'),
                comment: commentElem ? HTMLDecode(commentElem.innerHTML) : '',
                metaData: metaData
            };
            this.data.flows.push(flowObj);
        }

        // Sort
        this.data.flows.sort(function(flow1, flow2) {
            if (flow2.dest < flow1.dest) return 1;
            else if (flow2.dest > flow1.dest) return -1;
            else {
                if (flow2.source < flow1.source) return 1;
                else if (flow2.source > flow1.source) return -1;
                else return 0;
            }
        });
        this.initFlows();

        // Init Tags
        // for (const i in edges) {
        //     if (!edges.hasOwnProperty(i)) continue;
        //     const edge = edges[i];
        //     const $flowDiv = $('#icc-flow-{0}'.format(i));
        //     this.initXMLTags(edge.getElementsByTagName('tags')[0], $flowDiv);
        //     this.countSelectedTags($($flowDiv.find('label')[0]));
        // }
        return true;
    },

    importXML: function() {
        const _this = this;
        const $xmlCode = $('#xmlCode');
        $xmlCode.val('');
        const $xmlModal = $('#xmlModal');
        const $loading = $xmlModal.find('.modal-title-loading');
        const $confirmBtn = $xmlModal.find('.j-confirm');
        const xmlModal = new bootstrap.Modal($xmlModal);
        $xmlModal.find('.modal-title-text').html('导入 XML');
        $confirmBtn.off('click').on('click', function() {
            $loading.show();
            $confirmBtn.prop('disabled', true);
            window.setTimeout(function() {
                if (_this._importXML($xmlCode.val())) {
                    $loading.hide();
                    $confirmBtn.prop('disabled', false);
                    xmlModal.hide();
                }
            }, 200);
        });
        xmlModal.toggle();
    },

    makeToast: function(msg, duration = 3, bgClass = 'bg-success', iconClass = 'bi-check-circle-fill') {
        const $toast = $('.toast');
        const toast = bootstrap.Toast.getOrCreateInstance($toast);
        $('.toast-container').css('z-index', 7000);
        $toast.removeClass(function (index, className) {
            return (className.match (/(^|\s)bg-\S+/g) || []).join(' ');
        }).addClass(bgClass);
        $toast.find('.toast-icon').removeClass(function (index, className) {
            return (className.match (/(^|\s)bi-\S+/g) || []).join(' ');
        }).addClass(iconClass);
        $toast.find('.toast-msg').html(msg);
        toast.show(duration);
    },

    editFlow: function(flowId, flowObj, $basicInfoDiv) {
        console.log('editFlow', flowId, flowObj, $basicInfoDiv);
        const $flowDiv = $('#icc-flow-{0}'.format(flowId));
        flowObj.source = $basicInfoDiv.find('.icc-source-edit').val();
        flowObj.dest = $basicInfoDiv.find('.icc-dest-edit').val();
        flowObj.method = $basicInfoDiv.find('.icc-method-edit').val();
        $basicInfoDiv.find('.icc-source-show').html(flowObj.source);
        $basicInfoDiv.find('.icc-dest-show').html(flowObj.dest);
        $basicInfoDiv.find('.icc-method-show').html(flowObj.method);
        const flowName = this.getFlowName(flowObj);
        $flowDiv.find('.accordion-button[aria-controls="icc-flow-{0}-content"]'.format(flowId)).html(flowName);
    },

    addFlow: function() {
        const _this = this;
        const flowObj = {
            source: 'Source',
            dest: 'Destination',
            method: 'xxx'
        }
        _this.data.flows.push(flowObj);
        const $flowDiv = _this.initFlow(_this.data.flows.length - 1, flowObj);
        const $flowsDiv = $('#icc-flows');

        $flowDiv.find('.icc-tag-label').each(function(i, elem) {
            $(elem).on('click', function() {
                _this.countSelectedTags($flowDiv);
            })
        });
        $flowsDiv.append($flowDiv);
        _this.initCollapseBtn($flowDiv);
    },

    delFlow: function(flowId) {
        const _this = this;
        console.log('delFlow', flowId);
        if (!_this.data.flows.hasOwnProperty(flowId)) {
            return false;
        }
        _this.data.flows[flowId].isDelete = true;
        $('#icc-flow-{0}'.format(flowId)).remove();
    },

    initSearch: function() {
        const $search = $('.j-search');
        $search.on('input', function() {
            const keyword = $search.val();
            $('.icc-flow').each(function(i, elem) {
                const $flowDiv = $(elem);
                const name = $flowDiv.find('.accordion-button')[0].innerText;
                if (name.search(keyword) === -1) {
                    $flowDiv.hide();
                } else {
                    $flowDiv.show();
                }
            });
        });
    },

    initOption: function() {
        $('.option-onlyOne').on('change', function() {
            const $collDivs = $('.accordion-collapse');
            if ($(this).prop('checked')) {
                $collDivs.each(function(i, elem) {
                    const $elem = $(elem);
                    $elem.attr('data-bs-parent', $elem.attr('data-bs-parent-real'));
                });
            } else {
                $collDivs.each(function(i, elem) {
                    const $elem = $(elem);
                    $elem.attr('data-bs-parent', $elem.attr('data-bs-parent-fake'));
                });
            }
        });
    },

    initUI: function() {
        const _this = this;
        this.initFlows();
        this.initSearch()
        this.initOption();
        $('.j-export').on('click', function() {
            _this.exportXML();
        });
        $('.j-import').on('click', function() {
            _this.importXML();
        });
        $('.j-test').on('click', function() {
            _this.makeToast('test');
        });
        $('.j-save').on('click', function() {
            _this.saveLocal();
        });
        $('.j-save-read').on('click', function() {
            _this.readLocal();
        });
        $('.j-save-clear').on('click', function() {
            _this.clearLocal();
        });
        $('.j-add-flow').on('click', function() {
            _this.addFlow();
        })
    },

    init: function() {
        this.tagsCnt = 0;
        for (const i in this.data.tags) {
            if (!this.data.tags.hasOwnProperty(i)) continue;
            this.countTags(this.data.tags[i]);
        }
        this.initUI();
        this.readLocal();
    }
}

ICCTAGViewer.init();