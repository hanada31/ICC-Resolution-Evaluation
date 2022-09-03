"use strict";

/**
 * @name ICCTagViewer.BaseTag
 * @property {String} id ID of tag
 * @property {String} name Name of tag
 * @property {String} desc Description of tag
 * @property {Boolean} isShow Whether show the tag or not
 * @property {null|Array<ICCTagViewer.BaseTag>} subTags Array of sub tags
 */

/**
 * @name ICCTagViewer.OracleEdge
 * @property {String} source Source of OracleEdge
 * @property {String} dest Destination of OracleEdge
 * @property {String} method Method name of OracleEdge
 * @property {String} comment Comment of OracleEdge
 * @property {Boolean} intentFieldsIsICCBotNoResult is ICCBot has no intent fields resolution result
 * @property {Object} intentFields Intent fields
 * @property {String} checkIgnore Ignored checkers' name
 * @property {null|HTMLElement} metaData Extra node(s) under OracleEdge
 */

/**
 * @name ICCTagViewer.OracleEdgeCallLine
 * @property {String} src Source file path of CallLine
 * @property {String} start Start line number of CallLine
 * @property {String} end End line number of CallLine
 */

window.ICCTagViewer = window.ICCTagViewer ? window.ICCTagViewer : {};
window.ICCTagViewer.config = window.ICCTagViewer.config ? window.ICCTagViewer.config : {};
$.extend(window.ICCTagViewer.config, {
    srcBasePath: '/ICCViewer/source',
    labelBasePath: '/ICCViewer/label',
    dlApkBasePath: '/ICCViewer/download/apk',
    dlSrcPackBasePath: '/ICCViewer/download/srcPack',
    codeViewerDeltaLines: 5,
    sortMethod: 'source',
    supportLang: [
        { id: 'en-US', name: 'English' },
        { id: 'zh-CN', name: 'Chinese Simplified' },
    ],
    allIntentFields: {
        'action': "Action", 'category': "Category", 'flag': "Flag", 'data': "Data", 'type': "Type", 'extra': "Extra"
    }
});
$.extend(window.ICCTagViewer, {
    tagsCnt: 0,
    $copySrc: null,
    $copyCallPathSrc: null,
    filterShowTags: null,
    autoSaveInterval: null,
    enabledCheckers: [],
    data: { flows: [] },

    _T: function(txt) {
        if (this.i18n) {
            if (this.i18n.hasOwnProperty(txt)) return this.i18n[txt];
            else {
                console.log('[WARN] i18n translation missed: {0}'.format(txt));
                return txt;
            }
        }
        return txt;
    },

    // ==================== localStorage Begin ====================
    /**
     * Save edges info to localStorage
     */
    saveLocal: function() {
        localStorage.setItem('icc-xml', this._exportXML());
        const msg = 'Finished saving ICC XML to localStorage';
        console.log('[INFO] {0}'.format(this._T(msg)));
        this.makeToast(this._T(msg));
    },

    /**
     * Read edges info from localStorage
     */
    readLocal: function() {
        const xmlStr = localStorage.getItem('icc-xml');
        const _this = this;
        if (xmlStr) {
            let msg = 'Reading ICC XML from localStorage...';
            this.makeToast(this._T(msg), -1, 'bg-primary', 'bi-hourglass-split');
            window.setTimeout(function() {
                if (_this._importXML(xmlStr, true)) {
                    msg = 'Finished loading ICC XML from localStorage';
                    console.log('[INFO] {0}'.format(_this._T(msg)));
                    _this.makeToast(_this._T(msg));
                } else {
                    msg = 'Error while loading ICC XML from localStorage';
                    console.log('[ERROR] {0}'.format(_this._T(msg)));
                    _this.makeToast(_this._T(msg), -1, 'bg-danger', 'bi-x-circle-fill');
                }
            }, 500);
        }
    },

    /**
     * Clear edges info from localStorage
     */
    clearLocal: function() {
        localStorage.removeItem('icc-xml');
        this.makeToast(this._T('Finished clearing ICC XML from localStorage'));
    },

    // ==================== localStorage Ended ====================

    // =================== TAG Auto-check Start ===================
    /**
     * Get or toggle tag status
     * @param flowId {String} ICC flow ID
     * @param tagPath {String} ICC tag full path
     * @param isToggle {null|boolean} Whether toggle or not
     * @returns {null|boolean} Returns select status when get, or null for toggle
     */
    tagSelect: function(flowId, tagPath, isToggle = false) {
        const paths = tagPath.split('.');
        const pathEndpoint = paths.pop();
        let tagPrefix = 'icc-flow-{0}-{1}'.format(flowId.toString(), paths.join('-'));
        const $tagParentDiv = $('#{0}'.format(tagPrefix));
        if ($tagParentDiv.length < 1) {
            console.log('[WARN] [labelSelect] {0}'.format(
                this._T('label not found: {0}').format(tagPath)
            ));
            return null;
        }
        const $tagInput = $tagParentDiv.find('input[value="{0}"]'.format(pathEndpoint));
        if ($tagInput.length < 1) {
            console.log('[WARN] [labelSelect] {0}'.format(
                this._T('label input not found: {0}').format(tagPath)
            ));
            return null;
        }
        if (isToggle) $tagInput.click();
        else return $tagInput.prop('checked');
        return true;
    },

    /**
     * Check edge tag by comment
     * @param flowId {String} ICC flow ID
     * @param $container {jQuery} jQuery selector of ICC flow container
     */
    checkTags: function(flowId, $container) {
        const _this = this;
        if (!_this.checkers) return;
        if (!_this.getOption('checkTags', true)) return;

        const $commentInput = $container.find('.icc-comment-edit');
        const $commentCheckDiv = $container.find('.icc-tag-check-result');

        if (!_this.hasOption('enabledCheckers')) {
            if (_this.enabledCheckers.length < 1) {
                $.each(_this.checkers, function(i, checker) {
                    _this.enabledCheckers.push(checker.id);
                });
            }
        } else {
            _this.enabledCheckers = _this.getOption('enabledCheckers', []);
        }
        $commentCheckDiv.empty();
        let warnCnt = 0;
        $.each(_this.checkers, function(i, checker) {
            if (_this.enabledCheckers.indexOf(checker.id) === -1) return;
            const comment = $commentInput.val();
            const result = checker.func(flowId, comment);
            if (result.error) {
                if (result.type) {
                    const checkIgnores = _this.data.flows[flowId].checkIgnores;
                    if (checkIgnores && checkIgnores.indexOf(result.type) !== -1) {
                        console.log("Ignored checker: {0}".format(result.type));
                        return;
                    }
                }
                let tips = _this._T('Label: {0} [{1}] {2}');
                let tagShortNames = [];
                if (result.errorTags) {
                    result.errorTags.forEach((tagPath) => {
                        tagShortNames.push(_this._T(_this.getTagNameByPath(tagPath)));
                    });
                }
                if (result.msgPrefix === undefined) {
                    result.msgPrefix = _this._T('The label');
                }
                if (result.msgSuffix === undefined) {
                    result.msgSuffix = _this._T('seems conflict with the call path information, please check it again.');
                }
                tips = tips.format(
                    result.msgPrefix, tagShortNames.join(_this._T(', ')), result.msgSuffix
                );

                const $span = $('<span class="tag-warning" />');
                const $icon = $('<i class="bi bi-exclamation-triangle-fill" />');
                $span.append($icon);
                $span.append(tips);
                if (result.autoFix || result.ignorable !== false) $span.append(' | ');

                if (result.autoFix) {
                    const $ctrlFix = $('<a href="javascript:;" class="ms-1"></a>');
                    $ctrlFix.html(_this._T('Fix'));
                    $ctrlFix.on('click', function() {
                        result.errorTags.forEach((tagPath) => {
                            _this.tagSelect(flowId, tagPath, true);
                        });
                        $span.remove();
                    });
                    $span.append($ctrlFix);
                }
                if (result.ignorable !== false) {
                    const $ctrlIgnore = $('<a href="javascript:;" class="ms-1"></a>');
                    $ctrlIgnore.html(_this._T('Ignore'));
                    $ctrlIgnore.on('click', function() {
                        _this.data.flows[flowId].checkIgnores.push(result.type);
                        $span.remove();
                        warnCnt -= 1;
                        if (warnCnt < 1) {
                            $commentCheckDiv.hide();
                            $('#icc-flow-{0}'.format(flowId)).find('.tag-warning-icon').hide();
                        }
                    });
                    $span.append($ctrlIgnore);
                }
                $commentCheckDiv.append($span);
                warnCnt++;
            }
        });
        if (warnCnt > 0) {
            $commentCheckDiv.show();
            $('#icc-flow-{0}'.format(flowId)).find('.tag-warning-icon').show();
            console.log(_this._T("Finished checking edge #{0}, detected {1} problems").format(flowId, warnCnt));
        }
        else {
            $commentCheckDiv.hide();
            $('#icc-flow-{0}'.format(flowId)).find('.tag-warning-icon').hide();
        }
    },

    // =================== TAG Auto-check Ended ===================

    // =================== HTML Generator Begin ===================
    /**
     * Init div for single tag
     * @param tag {ICCTagViewer.BaseTag} Tag object
     * @returns {jQuery} jQuery selector of generated div
     */
    initSingleTag: function(tag) {
        const $div = $('<div class="form-check form-check-inline" />')
        const $label = $('<label class="form-check-label icc-tag-label" type="button" />');
        const $checkBox = $('<input class="form-check-input icc-tag" type="checkbox" />');
        $checkBox.attr('value', tag.id);
        $label.attr('title', '{0}: {1}'.format(tag.id, this._T(tag.desc)));
        $label.append($checkBox).append(this._T(tag.name));
        $div.append($label);
        if (tag.isShow === false) $div.hide();
        return $div;
    },

    /**
     * Init div for bootstrap accordion item
     * @param idPrefix {String} ID prefix of the item
     * @param itemTitle {String} Title of the item
     * @param bsParent {String} Accordion bs-parent
     * @param extraClass {String} Extra CSS class name
     * @returns {jQuery} jQuery selector of generated div
     */
    initAccordionItem: function(idPrefix, itemTitle, bsParent, extraClass = undefined) {
        const $itemDiv = $('<div class="accordion-item" />');
        $itemDiv.attr('id', idPrefix);
        if (extraClass) $itemDiv.addClass(extraClass);

        // Generate Header
        const $heading = $('<h2 class="accordion-header" />');
        $heading.attr('id', '{0}-heading'.format(idPrefix));

        const $btn = $('<button class="accordion-button collapsed btn" type="button" />');
        $btn.attr('data-bs-toggle', 'collapse')
            .attr('data-bs-target', '#{0}-content'.format(idPrefix))
            .attr('aria-expanded', false)
            .attr('aria-controls', '{0}-content'.format(idPrefix));
        if (extraClass) $btn.addClass(extraClass + '-accordion-btn');

        if (extraClass === 'icc-flow') {
            // Update the height of comment textarea
            $itemDiv[0].addEventListener('shown.bs.collapse', function() {
                $itemDiv.find('textarea').trigger('input');
            });

            // Generate OracleEdge title and tag counter
            const $flowNameSpan = $('<span class="flow-name" />');
            $flowNameSpan.html(itemTitle);
            $btn.append($flowNameSpan);
            const $b = $('<b />');
            $b.append(this._T("#labels: "));
            $b.append($('<span class="text-danger tags-num">0</span>'));
            $b.append("&nbsp;&nbsp;");
            $b.append(this._T("#callLines: "));
            $b.append($('<span class="text-danger callLines-num">0</span>'));
            const $tagWarningIcon = $('<i class="text-danger ms-2 bi bi-exclamation-triangle-fill tag-warning-icon" />');
            $b.append($tagWarningIcon.hide());
            $btn.append('&nbsp;/&nbsp;').append($b);
        } else if (extraClass === 'intent-fields') {
            const $titleSpan = $('<span />');
            $titleSpan.html(itemTitle);
            $btn.append($titleSpan);
            const $b = $('<b />');
            $b.append($('<span class="fields-num text-primary">0</span>'));
            $b.append("/" + Object.keys(this.config.allIntentFields).length);

            const $noResultTip = $('<span class="text-primary iccbot-no-res-tip">(ICCBot has no result)</span>');
            $btn.append('&nbsp;(').append($b).append(")&nbsp;").append($noResultTip.hide());
        } else {
            $btn.html(itemTitle);
        }
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

    /**
     * Init Div for tags (recursively)
     * @param tag {ICCTagViewer.BaseTag}
     * @param idPrefix {String} ID prefix of the tag
     * @param $rootDiv {jQuery} jQuery selector of root accordion div
     */
    initTagDiv: function(tag, idPrefix, $rootDiv) {
        const tagIdPrefix = '{0}-{1}'.format(idPrefix, tag.id);
        let $thisTagDiv;
        if (tag.hasOwnProperty('subTags')) {
            $thisTagDiv = this.initAccordionItem(tagIdPrefix, this._T(tag.name) + ' (' + tag.id + ')', idPrefix);
            const $thisTagContentDiv = $thisTagDiv.find('.accordion-collapse');

            // Has sub tags, build an accordion
            const $subTagsDiv = $('<div class="accordion container p-3" />');
            $subTagsDiv.attr('id', tagIdPrefix);

            // Build all sub tags
            for (const i in tag.subTags) {
                if (!tag.subTags.hasOwnProperty(i)) continue;
                const subTag = tag.subTags[i];
                this.initTagDiv(subTag, tagIdPrefix, $subTagsDiv);
            }
            $thisTagContentDiv.append($subTagsDiv);
        } else {
            // Has no sub tag, build label and input
            $thisTagDiv = this.initSingleTag(tag);
        }
        $rootDiv.append($thisTagDiv);
    },

    updateIntentFieldSel: function($rootDiv) {
        const noFields = Object.keys(this.config.allIntentFields);
        $.each(this.config.allIntentFields, function(cFieldId, cFieldName) {
            const cFieldHasSel = $rootDiv.find('option[value="' + cFieldId + '"]:selected').length > 0;
            if (cFieldHasSel) noFields.splice($.inArray(cFieldId, noFields), 1);
            $rootDiv.find('select').each(function(i, elem) {
                const $sel = $(elem);
                const $option = $sel.find('option[value="' + cFieldId +'"]');
                if ($sel.val() !== cFieldId && cFieldHasSel) {
                    $option.hide();
                } else {
                    $option.show();
                }
            });
        });
        $rootDiv.find('.has-if-input-hidden').val(
            noFields.length < Object.keys(this.config.allIntentFields).length ? 'true' : 'false'
        );
        $rootDiv.find('.btn-add-field').prop('disabled', noFields.length < 1);
        $rootDiv.find('.fields-num').html($rootDiv.find('select').length);

        const $container = $rootDiv.closest('.basic-info').parent();
        if ($container.find('.icc-tag-label').length > 0) {
            const flowId = $container.attr('data-flow-id');
            this.checkTags(flowId, $container);
        }
    },

    buildIntentFieldRow: function($rootDiv, fieldId, fieldName, fieldVal = '') {
        const _this = this;
        if (!this.config.allIntentFields.hasOwnProperty(fieldId)) {
            console.error("Invalid intent field id: " + fieldId);
            return null;
        }
        const $fieldDiv = $('<div class="row align-items-center intent-field-line" />');
        const $fieldDelBtn = $('<button class="btn btn-sm btn-danger ms-1 btn-intent-field-del" type="button"><i class="bi bi-dash-circle" /></button>');
        $fieldDelBtn.attr('title', this._T('Delete this field'));
        $fieldDelBtn.on('click', function() {
            $fieldDiv.remove();
            _this.updateIntentFieldSel($rootDiv);
        });

        const $fieldSel = $('<select class="form-select w-75 float-end intent-field-sel"/>');
        $.each(this.config.allIntentFields, function (cFieldId, cFieldName) {
            $fieldSel.append($('<option />')
                .attr('value', cFieldId)
                .prop('selected', cFieldId === fieldId)
                .html(cFieldName)
            );
        });
        $fieldSel.on('change', function() {
            _this.updateIntentFieldSel($rootDiv);
        });
        $fieldDiv.append($('<div class="col-2" />').append($fieldDelBtn).append($fieldSel));
        const $fieldInput = $('<input class="form-control align-items-end intent-field-val" type="text" />').val(fieldVal);

        const $container = $rootDiv.closest('.basic-info').parent();
        const flowId = $container.attr('data-flow-id');
        $fieldInput.on('blur', function() {
            _this.checkTags(flowId, $container);
        });

        $fieldDiv.append($('<div class="col-10" />').append($fieldInput));
        $rootDiv.find('.intent-fields-form').append($fieldDiv);
        return $fieldDiv;
    },

    initIntentFieldDiv: function(idPrefix, $rootDiv, flowObj) {
        const _this = this;
        const $innerDiv = this.initAccordionItem(
            idPrefix + '-intent-fields', this._T("Intent Fields"), idPrefix, 'intent-fields'
        );
        $rootDiv.append($innerDiv);
        const $innerContentDiv = $innerDiv.find('.accordion-collapse');
        const $formDiv = $('<div class="container p-3 intent-fields-form" />');
        $innerContentDiv.append($formDiv);

        const $ctrlDiv = $('<div class="row align-items-center mb-2 ps-2" />');
        $formDiv.append($ctrlDiv);

        const $addFieldBtn = $('<button class="col-auto btn btn-sm btn-success btn-add-field ms-1"></button>');
        $addFieldBtn.html(this._T("Add Field")).on('click', function() {
            const noFields = Object.keys(_this.config.allIntentFields);
            $rootDiv.find('select').each(function(i, elem) {
                noFields.splice($.inArray($(elem).val(), noFields), 1);
            });
            $addFieldBtn.prop('disabled', noFields.length <= 1);
            if (noFields.length > 0) {
                _this.buildIntentFieldRow($innerDiv, noFields[0], _this.config.allIntentFields[noFields[0]])
                _this.updateIntentFieldSel($innerDiv);
            }
        });
        $ctrlDiv.append($addFieldBtn);
        $formDiv.append($ctrlDiv);

        const $clearFieldBtn = $('<button class="col-auto btn btn-sm btn-danger btn-clear-field ms-1"></button>');
        $clearFieldBtn.html(this._T("Clear Fields")).on('click', function() {
            $rootDiv.find('.intent-field-line').remove();
            _this.updateIntentFieldSel($innerDiv);
        });
        $ctrlDiv.append($clearFieldBtn);

        const $hasIFInputHidden = $('<input class="has-if-input-hidden" type="hidden" value="true" />')
        if (flowObj.intentFieldsIsICCBotNoResult) {
            // $innerDiv.find('.iccbot-no-res-tip').show();
        }
        if (Object.keys(flowObj.intentFields).length > 0) {
            $formDiv.append($hasIFInputHidden);
        }
        else {
            $formDiv.append($hasIFInputHidden.attr('value', 'false'));
        }
        $.each(flowObj.intentFields, function(fieldId, fieldVal) {
            _this.buildIntentFieldRow($innerDiv, fieldId, _this.config.allIntentFields[fieldId], fieldVal);
        });
        this.updateIntentFieldSel($innerDiv);
    },

    /**
     * Add a call-line div for an OracleEdge
     * @param $baseDiv {jQuery} jQuery selector of ICC flow div
     * @param params {ICCTagViewer.OracleEdgeCallLine|{}} OracleEdgeCallLine object
     * @param $relatedDiv {null|jQuery} jQuery selector of another OracleEdge (will insert before if not null)
     */
    addCallLine: function($baseDiv, params, $relatedDiv = null) {
        const _this = this;
        const $lineDiv = $('<div class="row align-items-center my-2 call-line"/>');
        const $lineCol1 = $('<div class="col-sm-2">');
        $lineDiv.append($lineCol1);

        /********** Source code file path input **********/
        const $lineCol2 = $('<div class="col-sm-6">');
        const $lineSrcInput = $('<input class="form-control call-line-src" type="text" />');
        $lineSrcInput.attr('placeholder', this._T('Source file relative path'));
        $lineSrcInput.val(params.src ? params.src : '');
        $lineSrcInput.on('input', function() {
            let path = $lineSrcInput.val();
            if (path[0] !== '/') path = '/' + path;
            $lineSrcInput.val(path.replaceAll('\\', '/'));
        });
        $lineCol2.append($lineSrcInput);
        $lineDiv.append($lineCol2);

        /********** Start & end line number input **********/
        const $lineCol3 = $('<div class="col-sm-1">');
        const $lineCol4 = $('<div class="col-sm-1">');
        const $lineStartInput = $('<input class="form-control call-line-st" type="number" value="10" />');
        const $lineEndInput = $('<input class="form-control call-line-ed" type="number" value="11" />');
        $lineStartInput.val(params.start ? params.start : 1);
        $lineEndInput.val(params.end ? params.end : 1);
        $lineCol3.append($lineStartInput);
        $lineCol4.append($lineEndInput);

        // Line number checker
        const lineNumCheck = function() {
            let st = parseInt($lineStartInput.val());
            let ed = parseInt($lineEndInput.val());
            let lineCount = -1;
            if (codeViewer !== null) {
                lineCount = codeViewer.getModel().getLineCount();
            }
            if (st < 1) st = 1;
            else if (lineCount > -1 && st > lineCount) st = lineCount;
            $lineStartInput.val(st);
            if (ed < st) ed = st;
            else if (lineCount > -1 && ed > lineCount) ed = lineCount;
            $lineEndInput.val(ed);
        };

        // Check start & end line number when blur
        $lineStartInput.on('blur', lineNumCheck);
        $lineEndInput.on('blur', lineNumCheck);

        // Auto fill end line number when src & start are as same as any existing CallLine
        $lineStartInput.on('blur', function() {
            const $callLineDivs = $('.call-line');
            $callLineDivs.each(function(i, elem) {
                const $callLineDiv = $(elem);
                if ($callLineDiv[0] === $lineDiv[0]) return;
                if ($lineSrcInput.val() !== '' &&
                    $callLineDiv.find('.call-line-src').val() === $lineSrcInput.val() &&
                    $callLineDiv.find('.call-line-st').val() === $lineStartInput.val()) {
                    $lineEndInput.val($callLineDiv.find('.call-line-ed').val());
                    return true;
                }
            });
        });
        $lineDiv.append($lineCol3);
        $lineDiv.append($lineCol4);

        /********** CallLine controller: add button **********/
        const $lineCol5 = $('<div class="col-sm-2">');
        const $lineAddBtn = $('<button class="btn btn-sm btn-success ms-1" type="button"><i class="bi bi-plus-circle" /></button>');
        $lineAddBtn.attr('title', this._T('Add before'));
        $lineAddBtn.on('click', function() {
            _this.addCallLine($baseDiv, {}, $lineDiv);
            _this.countCallLines($baseDiv);
        });
        $lineCol5.append($lineAddBtn);
        $lineDiv.append($lineCol5);

        /********** CallLine controller: delete button **********/
        const $lineDelBtn = $('<button class="btn btn-sm btn-danger ms-1 btn-call-line-del" type="button"><i class="bi bi-dash-circle" /></button>');
        $lineDelBtn.attr('title', this._T('Delete this line'));
        $lineDelBtn.on('click', function() {
            $lineDiv.remove();
            _this.countCallLines($baseDiv);
        });
        $lineCol5.append($lineDelBtn);

        /********** CallLine controller: view button **********/
        const $lineViewBtn = $('<button class="btn btn-sm btn-primary ms-1 btn-view" type="button"></button>');
        $lineViewBtn.attr('title', this._T('View source code'));
        const $lineViewBtnI = $('<i class="bi bi-eye" />');
        $lineViewBtn.append($lineViewBtnI);
        $lineCol5.append($lineViewBtn);

        /********** CallLine controller: CodeViewer **********/
        let codeViewer = null;
        const codeViewerStates = {
            NOT_INIT: 0,
            LOADING: 1,
            READY: 2,
            HIDE: 3
        }
        let codeViewerState = codeViewerStates.NOT_INIT;
        let codeViewerSrcPath = null;
        let lineDecorations = [];
        const codeViewerId = 'code-container-' + (new Date()).getTime() + Math.round(Math.random() * 1000000);
        let $codeViewer = null;

        // Jump to marked line (by start & end line number)
        const revealLine = function() {
            if (codeViewerState < codeViewerStates.READY || !codeViewer) return;
            const lineCount = codeViewer.getModel().getLineCount();
            const st = Math.max(parseInt($lineStartInput.val()), 1);
            const ed = Math.min(parseInt($lineEndInput.val()), lineCount);
            let adj_st = Math.max(st - _this.config.codeViewerDeltaLines, 1);
            let adj_ed = Math.min(ed + _this.config.codeViewerDeltaLines, lineCount);
            let ct = Math.floor((ed - st) / 2);
            if (ct !== Math.ceil((ed - st) / 2)) {
                adj_ed += 1;
            }
            ct = ct + st;
            const lineHeight = codeViewer.getOption(monaco.editor.EditorOption.lineHeight);
            const h = Math.max((adj_ed - adj_st + 1) * lineHeight, 5 * lineHeight)
            $codeViewer.css('height', h + 'px');
            codeViewer.layout();
            codeViewer.revealLineInCenter(ct);
            lineDecorations = codeViewer.deltaDecorations(lineDecorations, [{
                range: new monaco.Range(st, 1, ed, 1),
                options: {isWholeLine: true, className: 'line-highlight'}
            }]);
        };
        $lineStartInput.on('change', function() {
            window.setTimeout(revealLine, 50);
        });
        $lineEndInput.on('change', function() {
            window.setTimeout(revealLine, 50);
        });
        $lineViewBtn.on('click', function() {
            const srcPath = _this.getSrcRootPath() + $lineSrcInput.val();
            if (codeViewerState === codeViewerStates.NOT_INIT ||
                (codeViewerState === codeViewerStates.HIDE && srcPath !== codeViewerSrcPath)
            ) {
                codeViewerState = codeViewerStates.LOADING;

                // Re-generate code viewer container
                if (srcPath !== codeViewerSrcPath) {
                    if ($codeViewer) $codeViewer.remove();
                    $codeViewer = $('<div class="col-sm-9 offset-sm-2 pb-3 overflow-hidden">Loading...</div>');
                    $codeViewer.attr('id', codeViewerId).css('height', '180px').css('border', '1px, solid');
                    $lineDiv.append($codeViewer);
                }

                // Load source code file
                codeViewerSrcPath = srcPath;
                $.ajax({
                    url: codeViewerSrcPath,
                    cache: true,
                    type: 'get',
                    dataType: 'text',
                    success: function(data) {
                        // Clear container and create editor
                        $codeViewer.html('');
                        codeViewer = monaco.editor.create($codeViewer.get(0), {
                            value: data,
                            minimap: {
                                enabled: false
                            },
                            language: 'java',
                            theme: 'code-block',
                            lineHeight: 18,
                        });
                        codeViewerState = codeViewerStates.READY;

                        // Jump to marked line
                        revealLine();
                        $lineViewBtnI.removeClass('bi-eye').addClass('bi-eye-fill');
                    },
                    error: function(event, xhr, options, exc) {
                        const msg = 'Failed to load source code file';
                        _this.makeToast(_this._T(msg), 3, 'bg-danger', 'bi-x-circle');
                        console.log('[ERROR] {0}: {1}'.format(_this._T(msg), codeViewerSrcPath));
                        $codeViewer.hide();
                        codeViewerState = codeViewerStates.NOT_INIT;
                    }
                });
            }
            else if (codeViewerState === codeViewerStates.READY) {
                $codeViewer.hide();
                $lineViewBtnI.removeClass('bi-eye-fill').addClass('bi-eye');
                codeViewerState = 3;
            }
            else if (codeViewerState === codeViewerStates.HIDE) {
                $codeViewer.show();
                revealLine();
                $lineViewBtnI.removeClass('bi-eye').addClass('bi-eye-fill');
                codeViewerState = 2;
            }
        });

        if ($relatedDiv) {
            $lineDiv.insertBefore($relatedDiv);
        } else {
            $baseDiv.append($lineDiv);
        }

        // Auto-focus on the src input
        $lineSrcInput.trigger('focus');
    },

    /**
     * Init div for tag selector
     * @param idPrefix {String} HTMLElement ID prefix of the OracleEdge
     * @param flowId {String} ID of the OracleEdge
     * @param flowObj {ICCTagViewer.OracleEdge} OracleEdge object
     * @returns {jQuery} jQuery selector of generated div
     */
    initTagSelector: function(idPrefix, flowId, flowObj) {
        const _this = this;
        const $container = $('<div class="container pb-3" />');
        $container.attr('data-flow-id', flowId);
        const $basicInfo = $('<div class="basic-info mb-3"/>');
        $container.append($basicInfo);
        const $ctrlDiv = $('<div class="row mb-3 align-items-center" />');

        // Controller
        // Copy Button
        const $copyBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 btn-copy"></button>');
        $copyBtn.html(_this._T('Copy Labels'));
        $copyBtn.on('click', function() {
            let $parent = $(this).parent();
            while (!$parent.hasClass('accordion-item')) $parent = $parent.parent();
            _this.$copySrc = $parent;
            $('.btn-paste').prop('disabled', false);
            _this.makeToast(_this._T('Successfully copied labels'), 2);
        });
        $ctrlDiv.append($copyBtn);

        // Paste Button
        const $pasteBtn = $('<button class="col-auto btn btn-sm btn-success ms-2 btn-paste" disabled></button>');
        $pasteBtn.html(this._T('Paste Labels'));
        $pasteBtn.on('click', function() {
            if (!_this.$copySrc) return;
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
            _this.makeToast(_this._T('Successfully pasted labels'), 2);

            // Update TAG checking status
            $copyTarget.find('.icc-comment-edit').trigger('blur');
        });
        $ctrlDiv.append($pasteBtn);

        // Collapse Button
        const $collapseBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 j-collapse"></button>');
        $collapseBtn.html(this._T('Expand All'));
        $ctrlDiv.append($collapseBtn);

        // Edit & Cancel Button
        const $editBtn = $('<button class="col-auto btn btn-sm btn-primary ms-2 j-edit"></button>');
        $editBtn.html(this._T('Edit Info'))
        const $cancelBtn = $('<button class="col-auto btn btn-sm btn-danger ms-2 j-edit-cancel"></button>');
        $cancelBtn.html(this._T('Discard Change'))
        $ctrlDiv.append($editBtn);
        $ctrlDiv.append($cancelBtn.hide());

        $editBtn.on('click', function() {
            if ($editBtn.hasClass('btn-primary')) {
                $container.find('.detail-show').hide();
                $container.find('.detail-edit').show();
                $editBtn.removeClass('btn-primary').addClass('btn-success');
                $editBtn.html(_this._T('Save Change'));
                $cancelBtn.show();
            } else {
                _this.editFlow(flowId, flowObj, $basicInfo);
                $container.find('.detail-show').show();
                $container.find('.detail-edit').hide();
                $editBtn.removeClass('btn-success').addClass('btn-primary');
                $editBtn.html(_this._T('Edit Info'));
                $cancelBtn.hide();
            }
        });
        $cancelBtn.on('click', function() {
            $container.find('.detail-show').show();
            $container.find('.detail-edit').hide();
            $editBtn.removeClass('btn-success').addClass('btn-primary');
            $editBtn.html(_this._T('Edit Info'));
            $cancelBtn.hide();
        });

        // Delete Button
        const $delBtn = $('<button class="col-auto btn btn-sm btn-danger ms-2 j-delete"></button>');
        $delBtn.html(this._T('Delete Edge'));
        $delBtn.on('click', function() {
            _this.delFlow(flowId);
        });
        $ctrlDiv.append($delBtn);
        $container.append($ctrlDiv);

        // Basic Information
        // Source
        const $sourceDiv = $('<div class="row align-items-center" />');
        const $sourceCol1 = $('<div class="col-auto">');
        const $sourceCol2 = $('<div class="col-lg mb-1 detail-edit">').hide();
        const $sourceCol3 = $('<div class="col-lg detail-show">');
        const $labelSource = $('<label class="col-form-label"></label>');
        $sourceCol1.append($labelSource.html(this._T('Source Class:')));
        const $sourceInput = $('<input class="form-control icc-source-edit" type="text" />').val(flowObj.source);
        $sourceCol2.append($sourceInput);
        const $sourceSpan = $('<span class="display-name icc-source-show" />').html(flowObj.source);
        $sourceCol3.append($sourceSpan);

        const $sourceCopyBtn = $('<button class="btn btn-sm btn-primary ms-1" type="button"></button>');
        const $sourceCopyBtnI = $('<i class="bi bi-file-earmark-text" />');
        $sourceCopyBtn.attr('title', this._T('Copy source class name'));
        $sourceCopyBtn.append($sourceCopyBtnI);
        $sourceCopyBtn.on('click', function() {
            const ipt = $sourceInput[0];
            ipt.textContent = flowObj.source;
            $sourceCol2.show();
            ipt.select();
            document.execCommand('copy');
            $sourceCol2.hide();
            _this.makeToast(_this._T('Class name copied'));
        });
        $sourceCol3.append($sourceCopyBtn);

        $sourceDiv.append($sourceCol1).append($sourceCol2).append($sourceCol3);
        $basicInfo.append($sourceDiv);

        // Target
        const $destDiv = $('<div class="row align-items-center" />');
        const $destCol1 = $('<div class="col-auto">');
        const $destCol2 = $('<div class="col-lg mb-1 detail-edit">').hide();
        const $destCol3 = $('<div class="col-lg detail-show">');
        const $labelDest = $('<label class="col-form-label"></label>');
        $destCol1.append($labelDest.html(this._T('Destination Class:')));
        const $destInput = $('<input class="form-control icc-dest-edit" type="text" />').val(flowObj.dest);
        $destCol2.append($destInput);
        const $destSpan = $('<span class="display-name icc-dest-show" />').html(flowObj.dest);
        $destCol3.append($destSpan);

        const $destCopyBtn = $('<button class="btn btn-sm btn-primary ms-1" type="button"></button>');
        const $destCopyBtnI = $('<i class="bi bi-file-earmark-text" />');
        $destCopyBtn.attr('title', this._T('Copy destination class name'));
        $destCopyBtn.append($destCopyBtnI);
        $destCopyBtn.on('click', function() {
            const ipt = $destInput[0];
            ipt.textContent = flowObj.source;
            $destCol2.show();
            ipt.select();
            document.execCommand('copy');
            $destCol2.hide();
            _this.makeToast(_this._T('Class name copied'));
        });
        $destCol3.append($destCopyBtn);

        $destDiv.append($destCol1).append($destCol2).append($destCol3);
        $basicInfo.append($destDiv);

        // Method
        const $methodDiv = $('<div class="row align-items-center" />');
        const $methodCol1 = $('<div class="col-sm-1">');
        const $methodCol2 = $('<div class="col-sm-6 detail-edit">').hide();
        const $methodCol3 = $('<div class="col-sm-6 detail-show">');
        const $labelMethod = $('<label class="col-form-label"></label>');
        $methodCol1.append($labelMethod.html(this._T('Method:')));
        const $methodInput = $('<input class="form-control icc-method-edit" type="text" />').val(flowObj.method);
        $methodCol2.append($methodInput);
        const $methodSpan = $('<span class="display-name icc-method-show" />').html(flowObj.method);
        $methodCol3.append($methodSpan);
        $methodDiv.append($methodCol1).append($methodCol2).append($methodCol3);
        $basicInfo.append($methodDiv.hide());

        // Comment
        const $commentDiv = $('<div class="row align-items-center" />');
        const $commentCol1 = $('<div class="col-sm-1">');
        const $commentCol2 = $('<div class="col-sm-11 detail">');
        const $labelComment = $('<label class="col-form-label"></label>');
        $commentCol1.append($labelComment.html(this._T('CallPath:')));
        const $commentInput = $('<textarea class="form-control icc-comment-edit" spellcheck="false"/>').val(flowObj.comment);
        $commentInput.autoHeight();
        $commentCol2.append($commentInput);
        $commentDiv.append($commentCol1).append($commentCol2);
        $basicInfo.append($commentDiv);

        // Lines
        const $linesDiv = $('<div class="call-lines" />');
        const $lineDiv = $('<div class="row align-items-center" />');
        const $lineFirstCol1 = $('<div class="col-sm-4">');
        const $lineFirstLabel = $('<label class="col-form-label"></label>');
        $lineFirstLabel.html(this._T('CallLines:'));
        $lineFirstCol1.append($lineFirstLabel);

        const $lineAddBtn = $('<button class="btn btn-sm btn-success ms-3 btn-call-line-add" type="button"><i class="bi bi-plus-circle" /></button>');
        $lineAddBtn.attr('title', this._T('Add at end'));
        $lineAddBtn.on('click', function() {
            _this.addCallLine($linesDiv, {});
            _this.countCallLines($basicInfo);
        });
        $lineFirstCol1.append($lineAddBtn);

        const $lineViewAllBtn = $('<button class="btn btn-sm btn-primary ms-1" type="button"></button>');
        const $lineViewAllBtnI = $('<i class="bi bi-eye" />');
        $lineViewAllBtn.attr('title', this._T('View All Code'));
        $lineViewAllBtn.append($lineViewAllBtnI);
        $lineViewAllBtn.on('click', function() {
            $linesDiv.find('.btn-view').click();
            if ($lineViewAllBtnI.hasClass('bi-eye')) $lineViewAllBtnI.removeClass('bi-eye').addClass('bi-eye-fill');
            else $lineViewAllBtnI.removeClass('bi-eye-fill').addClass('bi-eye');
        });
        $lineFirstCol1.append($lineViewAllBtn);

        // Line Copy
        const $lineCopyBtn = $('<button class="btn btn-sm btn-primary ms-1 btn-call-line-copy" type="button"><i class="bi bi-file-earmark-text" /></button>');
        $lineCopyBtn.attr('title', this._T('Copy Call Lines'));
        $lineCopyBtn.on('click', function() {
            let $parent = $(this).parent();
            while (!$parent.hasClass('accordion-item')) $parent = $parent.parent();
            _this.$copyCallPathSrc = $parent;
            _this.makeToast(_this._T('Successfully copied call lines'), 2);
        });
        $lineFirstCol1.append($lineCopyBtn);

        // Line Paste
        const $linePasteBtn = $('<button class="btn btn-sm btn-success ms-1 btn-call-line-paste" type="button"><i class="bi bi-clipboard-check" /></button>');
        $linePasteBtn.attr('title', this._T('Paste Call Lines'));
        $lineFirstCol1.append($linePasteBtn);

        $lineDiv.append($lineFirstCol1);
        $lineDiv.append($('<div class="col-sm-4 text-center"></div>').html(this._T('Source File')));
        $lineDiv.append($('<div class="col-sm-1 text-center"></div>').html(this._T('FromLine')));
        $lineDiv.append($('<div class="col-sm-1 text-center"></div>').html(this._T('ToLine')));
        $linesDiv.append($lineDiv);
        this.addCallLine($linesDiv, {});
        $basicInfo.append($linesDiv);

        // Line Paste Callback
        $linePasteBtn.on('click', function() {
            if (_this.$copyCallPathSrc != null) {
                _this.$copyCallPathSrc.find('.call-line').each(function(i, elem) {
                    const $lineDiv = $(elem);
                    _this.addCallLine($linesDiv, {
                        src: $lineDiv.find('.call-line-src').val(),
                        start: $lineDiv.find('.call-line-st').val(),
                        end: $lineDiv.find('.call-line-ed').val(),
                    });
                    _this.countCallLines($basicInfo);
                });
                _this.makeToast(_this._T('Successfully pasted call lines'), 2);
            }
        });

        // Counter
        const $counterDiv = $('<div class="row align-items-center mt-2" />');
        const $counterCol = $('<div class="col-auto"></div>');
        const $counter = $('<strong>{0}: <span class="text-danger tags-num">0</span>/{1}</strong>'.format(
            this._T('Selected labels count'), this.tagsCnt
        ));
        $counterCol.append($counter);
        $basicInfo.append($counterDiv.append($counterCol));

        // Generate Selector
        const $div = $('<div class="accordion" />');
        $div.attr('id', idPrefix + '-tags');
        for (const i in this.config.tags) {
            if (!this.config.tags.hasOwnProperty(i)) continue;
            const tag = this.config.tags[i];
            this.initTagDiv(tag, idPrefix, $div);
        }
        $container.append($div);

        // Intent fields
        const $intentFieldDiv = $('<div class="accordion mt-2" />');
        $container.append($intentFieldDiv);
        $intentFieldDiv.attr('id', idPrefix + '-intent-fields-accordion');
        this.initIntentFieldDiv(idPrefix, $intentFieldDiv, flowObj);

        // Comment Check
        const $commentCheckDiv = $('<div class="icc-tag-check-result row align-items-start mt-1 text-danger" />');
        $container.append($commentCheckDiv.hide());
        $commentInput.on('blur', function() {
            _this.checkTags(flowId, $container);
        });
        $div.find(':checkbox').on('change', function() {
            _this.checkTags(flowId, $container);
        });
        window.setTimeout(function() {
            _this.checkTags(flowId, $container);
        }, 500);
        return $container;
    },
    // =================== HTML Generator Ended ===================

    // ===================== Controller Start =====================
    /**
     * Parse XML callLines and create div
     * @param $linesDiv {jQuery} jQuery selector of callLines div
     * @param callLinesNode {HTMLElement} XML Node of callLines
     */
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

    /**
     * Generate callLines XML Node from HTML div
     * @param $linesDiv {jQuery} jQuery selector of callLines div
     * @returns {HTMLElement} XML Node of callLines
     */
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

    /**
     * Generate ICC OracleEdge Title by object (simplify class names)
     * @param flowObj {ICCTagViewer.OracleEdge} OracleEdge object
     * @returns {String} OracleEdge title
     */
    getFlowName: function(flowObj) {
        const source = flowObj.source.split('.').pop();
        const sourcePkg = flowObj.source.indexOf(')') > -1 ? flowObj.source.split(' ')[0] + ' ' : '';
        const dest = flowObj.dest.split('.').pop();
        const destPkg = flowObj.dest.indexOf(')') > -1 ? flowObj.dest.split(' ')[0] + ' ' : '';
        return '{0}{1} --> {2}{3}'.format(
            sourcePkg, source, sourcePkg === destPkg ? '' : destPkg, dest
        );
    },

    /**
     * Count selected tags and display the number in an OracleEdge div
     * @param $flowDiv {jQuery} jQuery selector of OracleEdge div
     */
    countSelectedTags: function($flowDiv) {
        const cnt = $flowDiv.find('input:checked').length;
        const $cntSpan = $flowDiv.find('.tags-num');
        $cntSpan.html(cnt);
        if (cnt < 1) $cntSpan.removeClass('text-primary').addClass('text-danger');
        else $cntSpan.removeClass('text-danger').addClass('text-primary');
    },

    /**
     * Count call lines and display the number in an OracleEdge div
     * @param $sourceDiv {jQuery} jQuery selector of any sub element of OracleEdge div
     */
    countCallLines: function($sourceDiv) {
        window.setTimeout(function() {
            // Find top-level OracleEdge div
            let $flowDiv = $sourceDiv;
            while (!$flowDiv.hasClass('icc-flow')) {
                $flowDiv = $flowDiv.parent();
            }
            const cnt = $flowDiv.find('.call-line-src').length;
            const $cntSpan = $flowDiv.find('.callLines-num');
            $cntSpan.html(cnt);
            if (cnt < 1) $cntSpan.removeClass('text-primary').addClass('text-danger');
            else $cntSpan.removeClass('text-danger').addClass('text-primary');
        }, 100);
    },

    /**
     * Count leaf nodes of tags (recursively)
     * @param tag {ICCTagViewer.BaseTag} Tag object
     */
    countTags: function(tag) {
        if (!tag.hasOwnProperty('subTags') && tag.isShow !== false) this.tagsCnt++;
        else {
            for (const i in tag.subTags) {
                if (!tag.subTags.hasOwnProperty(i)) continue;
                const subTag = tag.subTags[i];
                this.countTags(subTag);
            }
        }
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
            });
        });
        $flowsDiv.append($flowDiv);
        _this.initCollapseBtn($flowDiv);
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
        $flowDiv.find('.accordion-button[aria-controls="icc-flow-{0}-content"] .flow-name'.format(flowId)).html(flowName);
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

    _importXML: function(xmlStr, isFromLocal = false) {
        const _this = this;
        const xml = this.parserStringToXMLDOM(xmlStr);

        if (xml.getElementsByTagName('parsererror').length > 0) {
            const msg = 'XML Format Error';
            this.makeToast(_this._T(msg), 3,'bg-danger', 'bi-x-circle')
            return false;
        }

        const srcCodeBase = xml.getElementsByTagName('root')[0].getAttribute('srcCodeBase');
        this.setSrcRootPath(srcCodeBase, isFromLocal);
        const edges = xml.getElementsByTagName('OracleEdge');

        _this.data.flows.length = 0;
        $.each(edges, function(i, edge) {
            // Put into data
            const metaData = [];
            let commentElem = null;
            for (const j in edge.children) {
                if (!edge.children.hasOwnProperty(j)) continue;
                const child = edge.children[j];
                if (child.tagName === 'comment') {
                    commentElem = child;
                    continue;
                } else if (child.tagName === 'IntentFields') {
                    continue;
                }
                metaData.push(child);
            }
            const flowObj = {
                source: edge.getAttribute('source'),
                dest: edge.getAttribute('destination'),
                method: edge.getAttribute('method'),
                comment: commentElem ? HTMLDecode(commentElem.innerHTML) : '',
                intentFields: {},
                metaData: metaData
            };

            // Load Intent Fields
            const intentFieldsElem = edge.getElementsByTagName('IntentFields')[0];
            flowObj.intentFieldsIsICCBotNoResult = intentFieldsElem ? intentFieldsElem.getAttribute('isICCBotNoResult') === 'true' : false;
            const intentFieldElems = edge.getElementsByTagName('IntentField');
            $.each(intentFieldElems, function(i, ifElem) {
                flowObj.intentFields[ifElem.getAttribute('type')] = ifElem.getAttribute('value');
            });

            const checkIgnoreStr = edge.getElementsByTagName('tags')[0].getAttribute('checkIgnore');
            if (checkIgnoreStr) {
                flowObj.checkIgnores = checkIgnoreStr.split(', ');
            } else {
                flowObj.checkIgnores = [];
            }
            _this.data.flows.push(flowObj);
        });

        // Sort
        this.data.flows.sort(function(flow1, flow2) {
            if (_this.getOption('sortBy', 'source') === 'source') {
                if (flow2.source < flow1.source) return 1;
                else if (flow2.source > flow1.source) return -1;
                else {
                    if (flow2.dest < flow1.dest) return 1;
                    else if (flow2.dest > flow1.dest) return -1;
                    else return 0;
                }
            } else {
                if (flow2.dest < flow1.dest) return 1;
                else if (flow2.dest > flow1.dest) return -1;
                else {
                    if (flow2.source < flow1.source) return 1;
                    else if (flow2.source > flow1.source) return -1;
                    else return 0;
                }
            }
        });

        this.initFlows();
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
        $xmlModal.find('.modal-title-text').html(this._T('Import ICC XML'));
        $confirmBtn.off('click').on('click', function() {
            $loading.show();
            $confirmBtn.prop('disabled', true);
            window.setTimeout(function() {
                const res = _this._importXML($xmlCode.val());
                $loading.hide();
                $confirmBtn.prop('disabled', false);
                if (res) {
                    _this.makeToast(_this._T('Finished importing ICC XML'));
                    xmlModal.hide();
                }
            }, 200);
        });
        xmlModal.toggle();
    },

    _exportXML: function() {
        const xml = this.createXMLDOM();
        const _this = this;
        const rootNode = xml.createElement('root');
        rootNode.setAttribute('srcCodeBase', _this.getSrcRootPath().replace(_this.config.srcBasePath, ''));
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
            commentNode.textContent = $flowDiv.find('.icc-comment-edit').val();
            edgeNode.appendChild(commentNode);

            // Call Lines
            const $linesDiv = $flowDiv.find('.call-lines');
            const callLinesNode = this.callLineToXML($linesDiv);
            edgeNode.appendChild(callLinesNode);

            // Tags
            const tagsNode = xml.createElement('tags');
            if (flow.checkIgnores.length > 0) {
                tagsNode.setAttribute('checkIgnore', flow.checkIgnores.join(", "));
            }
            for (const j in this.config.tags) {
                if (!this.config.tags.hasOwnProperty(j)) continue;
                const tag = this.config.tags[j];
                this.exportTagsXML(tag, xml, [flowDomId], tagsNode);
            }
            edgeNode.appendChild(tagsNode);

            // Intent Fields
            const $ifLines = $flowDiv.find('.intent-field-line').toArray().sort(function(ifElem1, ifElem2) {
                const val1 = $(ifElem1).find('select').val();
                const val2 = $(ifElem2).find('select').val();
                return val1 > val2 ? 1 : (val1 === val2 ? 0 : -1);
            });
            const ifsNode = xml.createElement('IntentFields');
            if (flow.intentFieldsIsICCBotNoResult) {
                ifsNode.setAttribute('isICCBotNoResult', 'true');
                edgeNode.appendChild(ifsNode);
            }
            if ($ifLines.length > 0) {
                $.each($ifLines, function(i, elem) {
                    const $elem = $(elem);
                    const ifNode = xml.createElement('IntentField');
                    ifNode.setAttribute('type', $elem.find('select').val());
                    ifNode.setAttribute('value', $elem.find('input').val());
                    ifsNode.appendChild(ifNode);
                });
                edgeNode.appendChild(ifsNode);
            }
            rootNode.appendChild(edgeNode);
        }
        return this.parserXMLToString(xml);
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

    exportXML: function() {
        let resStr = this.formatXML(this._exportXML());
        resStr = '<?xml version="1.0" encoding="UTF-8"?>\n\n' + resStr;
        $('#xmlCode').val(resStr);
        const $xmlModal = $('#xmlModal');
        const xmlModal = new bootstrap.Modal($xmlModal);
        $xmlModal.find('.modal-title-text').html(this._T('Export ICC XML'));
        $xmlModal.find('.j-confirm').off('click').on('click', function() {
            xmlModal.hide();
        });
        xmlModal.toggle();
        return true;
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
            this.countCallLines($flowDiv);

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

    initAppSelector: function() {
        const _this = this;
        const $selector = $('#app-selector');
        const $custom = $selector.find('option[value="CUSTOM"]');
        if (this.preDefinedApps) {
            this.preDefinedApps.forEach(function(appItem) {
                const $option = $('<option />');
                $option.attr('value', appItem.pkgName)
                    .attr('app-name', appItem.name)
                    .attr('apk-file', appItem.apkFile)
                    .attr('src-pack-file', appItem.srcPackFile)
                    .attr('oracle-file', appItem.oracleFile ? appItem.oracleFile : appItem.pkgName + '_oracle.xml')
                    // .attr('data-package-name', appItem.pkgName)
                    .html(appItem.name.startsWith('=') ? _this._T(appItem.name) : appItem.name)
                    .insertBefore($custom);
            });
        }
        $selector.on('change', function() {
            const val = $(this).val();
            const $option = $(this).find('option:selected');
            const appName = $option.attr('app-name');

            const apkFile = $option.attr('apk-file');
            const srcPackFile = $option.attr('src-pack-file');
            const $dlLinkDiv = $('.app-dl-links').html('').hide();
            if (apkFile !== undefined) {
                const $apkLink = $('<a class="me-2" target="_blank" />');
                $apkLink.html(_this._T("Download APK"));
                $apkLink.attr('href', apkFile.indexOf('://') !== -1 ? apkFile : _this.config.dlApkBasePath + '/' + apkFile);
                $dlLinkDiv.append($apkLink);
            }
            if (srcPackFile !== undefined) {
                const $srcPackLink = $('<a class="me-2" target="_blank" />');
                $srcPackLink.html(_this._T("Download Source Pack"));
                $srcPackLink.attr('href', srcPackFile.indexOf('://') !== -1 ? srcPackFile : _this.config.dlSrcPackBasePath + '/' + srcPackFile);
                $dlLinkDiv.append($srcPackLink);
            }
            if ($dlLinkDiv.html() !== '') $dlLinkDiv.show();

            if (val === 'CUSTOM') $('.app-customRoot-container').show();
            else {
                $('.app-customRoot-container').hide();

                // Load XML
                _this.makeToast(
                    _this._T('Loading ICC XML for App {0}...').format(appName),
                    3000, 'bg-primary', 'bi-hourglass-split'
                );

                let xmlUrl = $option.attr('oracle-file');
                if (xmlUrl.indexOf('://') === -1) xmlUrl = _this.config.labelBasePath + '/' + xmlUrl;
                window.setTimeout(function() {
                    $.ajax({
                        url: xmlUrl + '?r=' + Math.random(),
                        type: 'get',
                        dataType: 'text',
                        success: function(data) {
                            _this._importXML(data);
                            _this.applyFilter();
                            _this.makeToast(_this._T('Finished loading ICC XML of App {0}').format(appName));
                        },
                        error: function(event, xhr, options, exc) {
                            _this.makeToast(
                                _this._T('Failed to load ICC XML for App {0}').format(appName),
                                -1, 'bg-danger', 'bi-x-circle-fill'
                            );
                        }
                    });
                }, 200);
            }
        });
    },

    initCollapseBtn: function($scope = undefined) {
        const _this = this;
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
                $parent.find('.accordion-item:visible .accordion-collapse').collapse('show');
                $btn.addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html(_this._T('Collapse All'));
                $parent.find('.j-collapse').addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html(_this._T('Collapse All'));
            } else {
                $parent.find('.accordion-item:visible .accordion-collapse').collapse('hide');
                $btn.removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html(_this._T('Expand All'));
                $parent.find('.j-collapse').removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html(_this._T('Expand All'));
            }
        })
    },

    initI18n: function(callback) {
        const _this = this;
        const lang = this.getOption('lang', this.config.supportLang[0].id);

        if (lang !== this.config.supportLang[0].id) {
            $.ajax({
                url: 'static/js/i18n/{0}.json'.format(lang),
                type: 'get',
                dataType: 'json',
                success: function(data) {
                    _this.i18n = data;
                    callback(true);
                },
                error: function(event, xhr, options, exc) {
                    console.error('[ERROR] Failed to load i18n config {0}.json'.format(lang));
                    console.error(event, xhr, options, exc);
                    callback(false);
                }
            })
        } else {
            callback(true);
        }
    },

    initAutoSave: function() {
        const _this = this;
        const isAutoSave = _this.getOption('autoSave', true);
        const durationMin = _this.getOption('autoSaveDurationMin', 5);
        if (isAutoSave) {
            if (_this.autoSaveInterval) window.clearInterval(_this.autoSaveInterval);
            _this.autoSaveInterval = window.setInterval(function() {
                _this.saveLocal();
            }, durationMin * 60 * 1000);
            _this.makeToast(_this._T('Enabled auto save'));
            console.info('Enabled auto save');
        } else {
            window.clearInterval(_this.autoSaveInterval);
            _this.makeToast(_this._T('Disabled auto save'));
            console.info('Disabled auto save');
        }
    },

    initUI: function() {
        const _this = this;
        this.initI18n(function(isSuccess) {
            $('.i18n-trans').each(function(i, elem) {
                $(elem).html(_this._T($(elem).html()));
            });
            $('#xmlCode').attr('placeholder', _this._T('Paste your ICC XML here'));

            _this.initFlows();
            _this.initSearch();
            _this.initAppSelector();
            _this.initAutoSave();

            $('.icc-local-warning').on('click', function() {
                _this.makeToast(
                    _this._T('All the changes are stored in the local storage of the browser so that the server-side is not affected. If you want to modify the data, please export the modified version and import it for use next time.'),
                    10, 'bg-primary'
                );
            })

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
            $('.j-filter').on('click', function() {
                _this.showFilter();
            });
            $('.j-summary').on('click', function() {
                _this.showSummary();
            });
            $('.j-config').on('click', function() {
                _this.showConfig();
            });
            $('.j-add-flow').on('click', function() {
                _this.addFlow();
            });

            _this.readLocal();
        });
    },

    initCodeViewer: function() {
        monaco.editor.defineTheme('code-block', {
            base: 'vs',
            inherit: true,
            rules: [],
            colors: {
                "editor.background": '#fafafa'
            }
        });
    },

    init: function() {
        this.initCodeViewer();
        this.initToast();
        this.tagsCnt = 0;
        for (const i in this.config.tags) {
            if (!this.config.tags.hasOwnProperty(i)) continue;
            this.countTags(this.config.tags[i]);
        }
        this.initUI();
    },

    /**
     * Get source code root path
     * @returns {String} Source code root path
     */
    getSrcRootPath: function() {
        return $('#app-customRoot').val();
    },

    /**
     * Set source code root path
     * @param srcPath {String} Source code root path
     * @param isFromLocal {Boolean} is loading from localStorage
     */
    setSrcRootPath: function(srcPath, isFromLocal = false) {
        const $selector = $('#app-selector');
        if (!srcPath) srcPath = '';

        let pkgName = '';
        const sp = srcPath.split('/');
        if (sp.length > 0) pkgName = sp[sp.length - 1];

        const option = $selector.find('option[value="{0}"]'.format(pkgName));
        if (option.length > 0) {
            if (!srcPath.endsWith($selector.val())) {
                $selector.val(pkgName);
                if (!isFromLocal) $selector.trigger('change');
            }
            // $('.app-customRoot-container').hide();
        } else {
            $selector.val('CUSTOM');
            $selector.trigger('change');
            // $('.app-customRoot-container').show();
        }
        $('#app-customRoot').val(this.config.srcBasePath + srcPath);
    },

    buildSummaryUL: function($baseUL, tag, isFilter = false) {
        const _this = this;
        const $title = $("<li />");
        const $titleSpan = $("<span />").html(_this._T(tag.name));
        $title.append($titleSpan);
        $baseUL.append($title);
        if (tag.hasOwnProperty('subTags')) {
            const $subUL = $('<ul />');
            $.each(tag.subTags, function(i, subTag) {
                _this.buildSummaryUL($subUL, subTag, isFilter);
            });
            $baseUL.append($subUL);
        } else {
            const $cntSpan = $('<span />');
            const cnt = $('.icc-flow input[value="' + tag.id + '"]:checked').length;
            $cntSpan.html(cnt).addClass(cnt > 0 ? 'text-primary' : 'text-danger');
            $titleSpan.html($titleSpan.html() + ': ');
            $title.append($('<b />').append($cntSpan));
            if (isFilter) {
                const chk = (_this.filterShowTags ? _this.filterShowTags.indexOf(tag.id) > -1 : true);
                const $sel = $('<input class="me-1 tag-checkbox" type="checkbox" />')
                    .attr('data-tag-id', tag.id)
                    .prop('checked', chk);
                $sel.insertBefore($titleSpan);
            }
        }
    },

    showSummary: function(isFilter = false) {
        const _this = this;
        const $summaryDiv = $('<div class="row" />');

        $.each(_this.config.tags, function(i, tag) {
            const $colDiv = $('<div class="col-auto" />');
            const $rootUL = $('<ul />');
            _this.buildSummaryUL($rootUL, tag, isFilter);
            $colDiv.append($rootUL);
            $summaryDiv.append($colDiv);
        });

        const $summaryModal = $('#commonModal');
        const summaryModal = new bootstrap.Modal($summaryModal);
        $summaryModal.find('.modal-title-text').html(isFilter ? this._T('Filter Setting') : this._T('ICC Summary'));
        $summaryModal.find('.modal-body').html('').append($summaryDiv);

        // Hide cancel button
        $summaryModal.find('.j-cancel').hide();
        $summaryModal.find('.modal-extra-buttons').html('');

        if (!isFilter) {
            $summaryModal.find('.j-confirm').off('click').on('click', function() {
                summaryModal.toggle();
            });
        } else {
            // Build buttons
            const $selAllBtn = $('<button class="btn btn-primary j-selAll ms-2" />')
                .html(_this._T("Select All"))
                .off('click').on('click', function() {
                    $summaryDiv.find('.tag-checkbox').prop('checked', true);
                });
            const $revSelAllBtn = $('<button class="btn btn-primary j-revSelAll ms-2" />')
                .html(_this._T("Reverse All"))
                .off('click').on('click', function() {
                    const $checkBoxes = $summaryDiv.find('.tag-checkbox');
                    $checkBoxes.each(function (i, checkBox) {
                        $(checkBox).prop('checked', !$(checkBox).prop('checked'));
                    });
                });
            const $clearBtn = $('<button class="btn btn-danger j-clear ms-2" />')
                .html(_this._T("Clear Filter"))
                .off('click').on('click', function() {
                    _this.clearFilter();
                    summaryModal.hide();
                });
            $summaryModal.find('.modal-extra-buttons')
                .append($selAllBtn).append($revSelAllBtn).append($clearBtn);
            $summaryModal.find('.j-confirm').off('click').on('click', function() {
                _this.applyFilter();
                summaryModal.hide();
            });
        }
        summaryModal.toggle();
        return $summaryModal;
    },

    showFilter: function() {
        const _this = this;
        const $filterModal = this.showSummary(true);
        const $extraFiltersContainer = $('<div class="row" />');
        const $extraFiltersDiv = $('<div class="col-12" />');

        const $hasFieldCheckbox = $('<input type="checkbox" id="config-has-intent-field" data-tag-id="filter.hasIntentField" />');
        $hasFieldCheckbox.prop('checked', _this.filterShowTags && _this.filterShowTags.indexOf('filter.hasIntentField') > -1);
        const $hasFieldLabel = $('<label for="config-has-intent-field" class="ms-1" />')
        $hasFieldLabel.html(_this._T("Show only edges with intent field"));
        $extraFiltersDiv.append($hasFieldCheckbox).append($hasFieldLabel);

        $extraFiltersContainer.append($extraFiltersDiv);
        $filterModal.find('.modal-body').append($extraFiltersContainer);
    },

    applyFilter: function() {
        const _this = this;
        const $filterDiv = $('#commonModal');
        if (!_this.filterShowTags) _this.filterShowTags = [];
        else _this.filterShowTags.length = 0;
        $filterDiv.find('input[type="checkbox"]:checked').each(function(i, elem) {
            _this.filterShowTags.push($(elem).attr('data-tag-id'));
        });
        console.log('showTags', _this.filterShowTags);
        if (_this.filterShowTags.length === 0) {
            $('.filter-tip').hide();
            return;
        } else {
            $('.filter-tip').show();
        }
        $('.icc-flow').each(function(i, flowDiv) {
            const $flowDiv = $(flowDiv);
            let flag = false;
            let flagTag = [];

            if (_this.filterShowTags.indexOf('filter.hasIntentField') !== -1 &&
                    $flowDiv.find('.has-if-input-hidden[value="true"]').length === 0) {
                flagTag.push('filter.hasIntentField');
            }
            if (flagTag.length < 1) {
                _this.filterShowTags.some(function (tagId, i) {
                    if (tagId === 'filter.hasIntentField' &&
                        $flowDiv.find('.has-if-input-hidden[value="true"]').length > 0) {
                        flag = true;
                        flagTag.push(tagId);
                        return true;
                    } else if ($flowDiv.find('input[type="checkbox"][value="' + tagId + '"]:checked').length > 0) {
                        flag = true;
                        flagTag.push(tagId);
                        return true;
                    }
                });
            }
            // console.log('flagTag', flagTag);
            !flag ? $flowDiv.hide() : $flowDiv.show();
        });
    },

    clearFilter: function() {
        $('.filter-tip').hide();
        $('.icc-flow').show();
    },

    buildCheckerConfigDiv: function(currCheckTags) {
        const _this = this;
        if (!_this.hasOption('enabledCheckers')) {
            $.each(_this.checkers, function(i, checker) {
                _this.enabledCheckers.push(checker.id);
            })
        } else {
            _this.enabledCheckers = _this.getOption('enabledCheckers', []);
        }
        const $div = $('<div class="checker-config"/>');

        const $checkLabel = $('<label for="option-checkTags" class="form-label" />');
        const $checkInput = $('<input class="form-check-input me-2" id="option-checkTags" type="checkbox" />');
        $checkInput.prop('checked', currCheckTags);
        const $checkOption = $('<div class="form-check-inline" />');
        $checkLabel.append($checkInput).append(_this._T("Enable Label Checking"));
        $checkOption.append($checkLabel);

        const $selAllA = $('<a class="ms-2" href="javascript:;" />').html(_this._T("Select All"));
        const $revSelA = $('<a class="ms-2" href="javascript:;" />').html(_this._T("Reverse All"));
        $checkOption.append($selAllA).append($revSelA);
        $div.append($checkOption);

        const $checkersDiv = $('<div class="row checkers-config" />');
        if (_this.checkers) {
            let totalCheckerCnt = 0;
            $.each(_this.checkers, function(i, checker) {
                if (checker.isPublic !== false) totalCheckerCnt++;
            });
            const cols = 3;
            const $colDivs = [];
            for (let i = 0; i < cols; i++) {
                $colDivs.push($('<div class="col-auto" />'));
            }
            const cntPerCol = Math.ceil(totalCheckerCnt / cols);
            let ri = 0;
            $.each(_this.checkers, function(i, checker) {
                if (checker.isPublic !== false) {
                    let $colDiv = $colDivs[Math.floor(ri / cntPerCol)];
                    const $checkerLabel = $('<label class="form-label ms-2" />');
                    $checkerLabel.attr('title', _this._T(checker.desc));
                    const $checkerInput = $('<input class="form-check-input checker-checkbox me-1" type="checkbox" />');
                    $checkerInput.val(checker.id);
                    $checkerInput.prop('checked', _this.enabledCheckers.indexOf(checker.id) > -1);
                    $checkerLabel.append($checkerInput).append(_this._T(checker.name));
                    $colDiv.append($checkerLabel).append('<div class="clearfix" />');
                    ri++;
                }
            });
            for (let i = 0; i < cols; i++) {
                $checkersDiv.append($colDivs[i]);
            }
        }
        $div.append($checkersDiv);
        if (!currCheckTags) $checkersDiv.hide();

        $checkInput.on('change', function() {
            const sel = $checkInput.prop('checked');
            sel ? $checkersDiv.slideDown() : $checkersDiv.slideUp();
        });
        $selAllA.on('click', function() {
            $checkersDiv.find('input').prop('checked', true);
        });
        $revSelA.on('click', function() {
            $checkersDiv.find('input').each(function(i, elem) {
                const $box = $(elem);
                $box.prop('checked', !$box.prop('checked'));
            })
        })

        return $div;
    },

    showConfig: function() {
        const _this = this;
        const $configModal = $('#commonModal');
        const configModal = new bootstrap.Modal($configModal);
        $configModal.find('.modal-title-text').html(this._T('Viewer Config'));
        const $modalBody = $configModal.find('.modal-body');
        $modalBody.html('');

        // Hide extra buttons
        $configModal.find('.modal-extra-buttons').html('');

        // Show cancel button
        $configModal.find('.j-cancel').show();

        // Load config values
        const currSortBy = _this.getOption('sortBy', 'source');
        const currLang = _this.getOption('lang', this.config.supportLang[0].id);
        const currAutoSave = _this.getOption('autoSave', true);
        const currCheckTags = _this.getOption('checkTags', true);
        const currAutoSaveDuration = _this.getOption('autoSaveDuration', 5);

        // Sort order
        const $sortByLabel = $('<label class="form-label" />').html(_this._T("Sort by"));
        const $sortBySelect = $('<select class="form-select" id="option-sortBy" />');
        $sortBySelect.append($('<option value="source" />').html(_this._T("Source")));
        $sortBySelect.append($('<option value="destination" />').html(_this._T("Destination")));
        $sortByLabel.append($sortBySelect);
        $modalBody.append($sortByLabel);

        // Language
        const $langLabel = $('<label class="form-label ms-4" />').html(_this._T("Language"));
        const $langSelect = $('<select class="form-select" id="option-lang" />');
        _this.config.supportLang.forEach(function(langItem) {
            $langSelect.append($('<option />').attr('value', langItem.id).html(_this._T(langItem.name)));
        });
        $langLabel.append($langSelect);
        $modalBody.append($langLabel);

        // Auto-Save duration
        const $saveDurationLabel = $('<label class="form-label ms-4" />').html(_this._T("Auto Save Duration (Minutes)"));
        const $saveDurationInput = $('<input class="form-control" id="option-autoSaveDuration" type="number" />');
        $saveDurationInput.css('max-width', '100px');
        $saveDurationLabel.append($saveDurationInput);
        $modalBody.append($saveDurationLabel);

        // Auto-Save
        const $saveLabel = $('<label for="option-autoSave" class="form-label ms-2" />').html(_this._T("Auto Save"));
        const $saveInput = $('<input class="form-check-input" id="option-autoSave" type="checkbox" />');
        const $saveOption = $('<div class="form-check-inline ms-4" />');
        $saveOption.append($saveInput).append($saveLabel);
        $saveInput.off('change').on('change', function() {
            if ($saveInput.prop('checked')) $saveDurationInput.prop('disabled', false);
            else $saveDurationInput.prop('disabled', true);
        });
        $modalBody.append($saveOption);

        // Checker
        const $checkerDiv = _this.buildCheckerConfigDiv(currCheckTags);
        const $checkEnableInput = $checkerDiv.find('#option-checkTags');
        $modalBody.append($checkerDiv);

        $sortBySelect.val(currSortBy);
        $langSelect.val(currLang);
        $saveDurationInput.val(currAutoSaveDuration);
        $saveInput.prop('checked', currAutoSave);
        if (currAutoSave) $saveDurationInput.prop('disabled', false);
        else $saveDurationInput.prop('disabled', true);

        // Confirm
        $configModal.find('.j-confirm').off('click').on('click', function() {
            const newSortBy = $sortBySelect.val();
            const newLang = $langSelect.val();
            const newAutoSaveDuration = parseInt($saveDurationInput.val());
            const newAutoSave = $saveInput.prop('checked');
            const newCheckTags = $checkEnableInput.prop('checked');
            const isReload = (newLang !== currLang);
            const isSaveAndRead = (newSortBy !== currSortBy);

            const newEnabledCheckers = [];
            $checkerDiv.find('.checker-checkbox:checked').each(function(i, elem) {
                newEnabledCheckers.push($(elem).val());
            });
            const isCheckerChanged = JSON.stringify(newEnabledCheckers) !== JSON.stringify(_this.enabledCheckers);

            _this.setOption('sortBy', newSortBy);
            _this.setOption('lang', newLang);
            _this.setOption('autoSaveDuration', newAutoSaveDuration);
            _this.setOption('autoSave', newAutoSave);
            _this.setOption('checkTags', newCheckTags);
            _this.setOption('enabledCheckers', newEnabledCheckers);

            if (newCheckTags !== currCheckTags || isCheckerChanged) {
                if (newCheckTags) {
                    $('.icc-comment-edit').trigger('blur');
                } else {
                    $('.icc-tag-check-result').hide();
                    $('.tag-warning-icon').hide();
                }
            }

            if (newAutoSave !== currAutoSave || newAutoSaveDuration !== currAutoSaveDuration) {
                _this.initAutoSave();
            }

            if (isReload) {
                if ($('#app-selector').val() !== 'EMPTY') _this.saveLocal();
                window.location.reload();
            }
            else if (isSaveAndRead) {
                _this.saveLocal();
                _this.readLocal();
            }
            _this.makeToast(_this._T('Config saved'));
            configModal.hide();
        });

        configModal.toggle();
    },
    // ===================== Controller Ended =====================

    // ======================= Util Start =========================
    /**
     * Create XML Document via Browser API
     * @returns {XMLDocument|null} XML Document
     */
    createXMLDOM: function() {
        let xmlDOM;
        if (window.ActiveXObject) {
            xmlDOM = new ActiveXObject('Microsoft.XMLDOM');
        } else if (document.implementation
            && document.implementation.createDocument) {
            xmlDOM = document.implementation.createDocument('', '', null);
        } else {
            const msg = 'Sorry, your browser does not support XML Document Creation';
            alert(this._T(msg));
            return null;
        }
        return xmlDOM;
    },

    toastTimeoutHandler: null,
    toastHidingFlag: false,
    toastInstance: null,
    initToast: function() {
        const _this = this;
        const $toast = $('.toast');
        this.toastInstance = new bootstrap.Toast($toast, {
            autohide: false
        });
        $toast.get(0).addEventListener('hide.bs.toast', function() {
            _this.toastHidingFlag = true;
        });
        $toast.get(0).addEventListener('hidden.bs.toast', function() {
            _this.toastHidingFlag = false;
        });
    },
    /**
     * Make toast message
     * @param msg {String} Message
     * @param duration {Number} Duration seconds (defaults as 3)
     * @param bgClass {String} Background CSS class name (defaults as bg-success)
     * @param iconClass {String} Icon CSS class name (defaults as bi-check-circle-fill)
     */
    makeToast: function(msg, duration = 3, bgClass = 'bg-success', iconClass = 'bi-check-circle-fill') {
        const _this = this;
        if (!this.toastInstance) {
            console.log('[ERROR] {0}'.format(this._T('Toast instance not created')));
            return;
        }
        const $toast = $('.toast');
        const toast = this.toastInstance;
        if (this.toastHidingFlag) {
            window.setTimeout(function() {
                _this.makeToast(msg, duration, bgClass, iconClass);
            }, 500);
            return;
        }
        $('.toast-container').css('z-index', 7000);
        $toast.removeClass(function (index, className) {
            return (className.match (/(^|\s)bg-\S+/g) || []).join(' ');
        }).addClass(bgClass);
        $toast.find('.toast-icon').removeClass(function (index, className) {
            return (className.match (/(^|\s)bi-\S+/g) || []).join(' ');
        }).addClass(iconClass);
        $toast.find('.toast-msg').html(msg);
        toast.show();
        if (this.toastTimeoutHandler) {
            window.clearTimeout(this.toastTimeoutHandler);
        }
        if (duration > 0) {
            this.toastTimeoutHandler = window.setTimeout(function() {
                toast.hide();
            }, duration * 1000);
        }
    },

    /**
     * Parse XML String to XML Document
     * @param str {String} XML String
     * @returns {Document|XMLDocument} XML Document
     */
    parserStringToXMLDOM: function(str) {
        const parser = new DOMParser();
        return parser.parseFromString(str, 'text/xml');
    },

    /**
     * Encode XML Document to String
     * @param xmlDOM {XMLDocument} XML Document
     * @returns {String} XML String
     */
    parserXMLToString: function(xmlDOM) {
        if (window.ActiveXObject) {
            return xmlDOM.xml;
        } else if (document.implementation
            && document.implementation.createDocument) {
            return new XMLSerializer().serializeToString(xmlDOM);
        }
    },

    /**
     * Format XML String
     * @param xml {String} XML String
     * @param tab {String} TAB characters (defaults as 2 blanks)
     * @param nl {String} Line ending (defaults as \n)
     * @returns {String} Formatted XML String
     */
    formatXML: function(xml, tab = '  ', nl = '\n') {
        let formatted = '', indent = '';
        const nodes = xml.slice(1, -1).split(/>\s*</);
        if (nodes[0][0] === '?') formatted += '<' + nodes.shift() + '>' + nl;
        for (let i = 0; i < nodes.length; i++) {
            let node = nodes[i];
            if (node[0] === '/') indent = indent.slice(tab.length);
            if (node[node.length - 1] === '/' && node[node.length - 2] !== ' ') node = node.slice(0, node.length - 1) + ' /';
            formatted += indent + '<' + node + '>' + nl;
            if (node[0] !== '/' && node[node.length - 1] !== '/' && node.indexOf('</') === -1) indent += tab;
        }
        return formatted;
    },

    /**
     * Set option (save by localStorage)
     * @param optionId {String} Option ID
     * @param optionVal {any} Option value (will be stringify)
     */
    setOption: function(optionId, optionVal) {
        localStorage.setItem('icc-{0}'.format(optionId), JSON.stringify(optionVal));
    },

    /**
     * Get option (from localStorage)
     * @param optionId {String} Option ID
     * @param defaultVal {null|any} Option default value
     * @returns {null|String} Option value (parse by JSON)
     */
    getOption: function(optionId, defaultVal = null) {
        let v = localStorage.getItem('icc-{0}'.format(optionId));
        if (defaultVal && v !== null) {
            const typeVerify = typeof JSON.parse(v) === typeof defaultVal;
            if (!typeVerify) {
                localStorage.setItem('icc-{0}'.format(optionId), defaultVal);
                v = defaultVal;
                console.warn('option [' + optionId + '] type verify failed, reset to default value:', v);
            }
        }
        return v === null ? defaultVal : JSON.parse(v);
    },

    hasOption: function(optionId) {
        return localStorage.getItem('icc-{0}'.format(optionId)) !== null;
    },

    /**
     * Get tag name by tag path
     * @param tagPath {String} Full tag path
     * @returns {null|String} Tag name
     */
    getTagNameByPath: function(tagPath) {
        const _this = this;
        let obj = null;
        const pathArr = tagPath.split('.');

        // Top-level
        this.config.tags.some(function(tagObj) {
            if (tagObj.id === pathArr[0]) {
                obj = tagObj;
                return true;
            }
        });

        // Sub tags
        if (obj) {
            pathArr.slice(1).some(function(tagId) {
                const res = obj.subTags.some(function(tagObj) {
                    if (tagObj.id === tagId) {
                        obj = tagObj;
                        return true;
                    }
                });
                if (!res) {
                    console.log("[ERROR] {0}".format(
                        _this._T('Failed to get label name for path: {0}').format(tagPath)
                    ));
                    obj = null;
                    return true;
                }
            });
        }
        return obj ? obj.name : null;
    }
    // ======================= Util Ended =========================
});
ICCTagViewer.init();
