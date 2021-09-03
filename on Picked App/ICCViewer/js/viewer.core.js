"use strict";

/**
 * @name ICCTagViewer.BaseTag
 * @property {String} id ID of tag
 * @property {String} name Name of tag
 * @property {String} desc Description of tag
 * @property {null|Array<ICCTagViewer.BaseTag>} subTags Array of sub tags
 */

/**
 * @name ICCTagViewer.OracleEdge
 * @property {String} source Source of OracleEdge
 * @property {String} dest Destination of OracleEdge
 * @property {String} method Method name of OracleEdge
 * @property {String} comment Comment of OracleEdge
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
    srcBasePath: '/ICCViewer/apps',
    codeViewerDeltaLines: 5,
    sortMethod: 'source',
    supportLang: [
        { id: 'en-US', name: 'ENG' },
        { id: 'zh-CN', name: '中文' },
    ]
});
$.extend(window.ICCTagViewer, {
    tagsCnt: 0,
    $copySrc: null,
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
                if (_this._importXML(xmlStr)) {
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
     * @param $commentInput {jQuery} jQuery selector of ICC flow comment input
     * @param $commentCheckDiv {jQuery} jQuery selector of ICC flow comment check div
     */
    checkTags: function(flowId, $commentInput, $commentCheckDiv) {
        const _this = this;
        if (!_this.getOption('checkTags', false)) return;
        $commentCheckDiv.empty();
        let warnCnt = 0;
        if (!_this.checkers) return;
        $.each(_this.checkers, function(i, elem) {
            const comment = $commentInput.val();
            const result = elem(flowId, comment);
            if (result.error) {
                let tips = _this._T('Label: {0} [{1}] {2}');
                let tagShortNames = [];
                if (result.errorTags) {
                    result.errorTags.forEach((tagPath) => {
                        tagShortNames.push(_this._T(_this.getTagNameByPath(tagPath)));
                    });
                }
                if (result.msgPrefix === undefined) {
                    result.msgPrefix = 'The label';
                }
                if (result.msgSuffix === undefined) {
                    result.msgSuffix = 'seems conflict with the call path information, please check it again.';
                }
                tips = tips.format(
                    _this._T(result.msgPrefix), tagShortNames.join(_this._T(', ')), _this._T(result.msgSuffix)
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
            const lineHeight = codeViewer.getOption(56);
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

        const $basicInfo = $('<div class="basic-info mb-3"/>');
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
        const $sourceCol2 = $('<div class="col-6 detail-edit">').hide();
        const $sourceCol3 = $('<div class="col-6 detail-show">');
        const $labelSource = $('<label class="col-form-label"></label>');
        $sourceCol1.append($labelSource.html(this._T('Source Class:')));
        const $sourceInput = $('<input class="form-control icc-source-edit" type="text" />').val(flowObj.source);
        $sourceCol2.append($sourceInput);
        const $sourceSpan = $('<span class="display-name icc-source-show" />').html(flowObj.source);
        $sourceCol3.append($sourceSpan);
        $sourceDiv.append($sourceCol1).append($sourceCol2).append($sourceCol3);
        $basicInfo.append($sourceDiv);

        // Target
        const $destDiv = $('<div class="row align-items-center" />');
        const $destCol1 = $('<div class="col-auto">');
        const $destCol2 = $('<div class="col-sm-6 detail-edit">').hide();
        const $destCol3 = $('<div class="col-sm-6 detail-show">');
        const $labelDest = $('<label class="col-form-label"></label>');
        $destCol1.append($labelDest.html(this._T('Destination Class:')));
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
        const $lineFirstCol1 = $('<div class="col-sm-2">');
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

        $lineDiv.append($lineFirstCol1);
        $lineDiv.append($('<div class="col-sm-6 text-center"></div>').html(this._T('Source File')));
        $lineDiv.append($('<div class="col-sm-1 text-center"></div>').html(this._T('FromLine')));
        $lineDiv.append($('<div class="col-sm-1 text-center"></div>').html(this._T('ToLine')));
        $linesDiv.append($lineDiv);
        this.addCallLine($linesDiv, {});
        $basicInfo.append($linesDiv);

        // Counter
        const $counterDiv = $('<div class="row align-items-center mt-2" />');
        const $counterCol = $('<div class="col-auto"></div>');
        const $counter = $('<strong>{0}: <span class="text-danger tags-num">0</span>/{1}</strong>'.format(
            this._T('Selected labels count'), this.tagsCnt
        ));
        $counterCol.append($counter);
        $basicInfo.append($counterDiv.append($counterCol));

        $container.append($basicInfo);

        // Generate Selector
        const $div = $('<div class="accordion" />');
        $div.attr('id', idPrefix);
        for (const i in this.config.tags) {
            if (!this.config.tags.hasOwnProperty(i)) continue;
            const tag = this.config.tags[i];
            this.initTagDiv(tag, idPrefix, $div);
        }
        $container.append($div);

        // Comment Check
        const $commentCheckDiv = $('<div class="icc-tag-check-result row align-items-start mt-1 text-danger" />');
        $container.append($commentCheckDiv.hide());
        $commentInput.on('blur', function() {
            _this.checkTags(flowId, $commentInput, $commentCheckDiv);
        });
        $div.find(':checkbox').on('change', function() {
            _this.checkTags(flowId, $commentInput, $commentCheckDiv);
        });
        window.setTimeout(function() {
            _this.checkTags(flowId, $commentInput, $commentCheckDiv);
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
        const dest = flowObj.dest.split('.').pop();
        // const method = flowObj.method.split('.').pop();
        // return '{0} --> {1} [{2}]'.format(source, dest, method);
        return '{0} --> {1}'.format(source, dest);
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
        if (!tag.hasOwnProperty('subTags')) this.tagsCnt++;
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

    _importXML: function(xmlStr) {
        const _this = this;
        const xml = this.parserStringToXMLDOM(xmlStr);

        if (xml.getElementsByTagName('parsererror').length > 0) {
            const msg = 'XML Format Error';
            this.makeToast(_this._T(msg), 3,'bg-danger', 'bi-x-circle')
            return false;
        }

        const srcCodeBase = xml.getElementsByTagName('root')[0].getAttribute('srcCodeBase');
        this.setSrcRootPath(srcCodeBase);
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
            if (_this.getOption('sortBy') === 'source') {
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
            for (const j in this.config.tags) {
                if (!this.config.tags.hasOwnProperty(j)) continue;
                const tag = this.config.tags[j];
                this.exportTagsXML(tag, xml, [flowDomId], tagsNode);
            }
            edgeNode.appendChild(tagsNode);
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

    initOptions: function() {
        const _this = this;
        // Sort by
        $('#option-sortBy').val(_this.getOption('sortBy', 'source')).on('change', function() {
            _this.setOption('sortBy', $(this).val());
            _this.saveLocal();
            _this.readLocal();
        });

        // Auto save
        let autoSaveInterval = null;
        let saveDuration = 5;
        $('#option-autoSave').prop('checked', _this.getOption('autoSave', true)).on('change', function() {
            const checked = $(this).prop('checked');
            _this.setOption('autoSave', checked);
            if (checked) {
                autoSaveInterval = window.setInterval(function() {
                    _this.saveLocal();
                }, saveDuration * 60 * 1000);
                _this.makeToast(_this._T('Enabled auto save'));
            } else {
                window.clearInterval(autoSaveInterval);
                _this.makeToast(_this._T('Disabled auto save'));
            }
        });

        // TAG Checkers
        $('#option-checkTags').prop('checked', _this.getOption('checkTags')).on('change', function() {
            const checked = $(this).prop('checked');
            _this.setOption('checkTags', checked);
            if (checked) {
                $('.icc-comment-edit').trigger('blur');
                _this.makeToast(_this._T('Enabled label checking'));
            } else {
                $('.icc-tag-check-result').hide();
                $('.tag-warning-icon').hide();
                _this.makeToast(_this._T('Disabled label checking'));
            }
        });

    },

    initAppSelector: function() {
        const _this = this;
        const $selector = $('#app-selector');
        const $custom = $selector.find('option[value="CUSTOM"]');
        if (this.preDefinedApps) {
            this.preDefinedApps.forEach(function(appItem) {
                const $option = $('<option />');
                $option.attr('value', appItem.path)
                    .attr('xml-url', appItem.xmlUrl)
                    .attr('app-name', appItem.name)
                    .html(appItem.name)
                    .insertBefore($custom);
            });
        }
        $selector.on('change', function() {
            const val = $(this).val();
            const $option = $(this).find('option:selected');
            const xmlUrl = $option.attr('xml-url');
            const appName = $option.attr('app-name');
            if (val === 'CUSTOM') $('.app-customRoot-container').show();
            else {
                $('.app-customRoot-container').hide();

                // Load XML
                _this.makeToast(
                    _this._T('Loading ICC XML for App {0}...').format(appName),
                    -1, 'bg-primary', 'bi-hourglass-split'
                );
                $.ajax({
                    url: xmlUrl,
                    type: 'get',
                    dataType: 'text',
                    success: function(data) {
                        _this._importXML(data);
                        _this.makeToast(_this._T('Finished loading ICC XML of App {0}').format(appName));
                    },
                    error: function(event, xhr, options, exc) {
                        _this.makeToast(
                            _this._T('Failed to load ICC XML for App {0}').format(appName),
                            -1, 'bg-danger', 'bi-x-circle-fill'
                        );
                    }
                });
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
                $parent.find('.accordion-collapse').collapse('show');
                $btn.addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html(_this._T('Collapse All'));
                $parent.find('.j-collapse').addClass('j-collapse-show')
                    .removeClass('btn-primary')
                    .addClass('btn-danger')
                    .html(_this._T('Collapse All'));
            } else {
                $parent.find('.accordion-collapse').collapse('hide');
                $btn.removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html(_this._T('Collapse All'));
                $parent.find('.j-collapse').removeClass('j-collapse-show')
                    .removeClass('btn-danger')
                    .addClass('btn-primary')
                    .html(_this._T('Collapse All'));
            }
        })
    },

    initI18n: function(callback) {
        const _this = this;
        const lang = this.getOption('lang', this.config.supportLang[0].id);

        // Lang Selector
        const $lang = $('#option-lang');
        $lang.html('');
        this.config.supportLang.forEach(function(langItem) {
            $lang.append($('<option />').attr('value', langItem.id).html(langItem.name));
        });
        $lang.val(lang);
        $lang.on('change', function() {
            _this.setOption('lang', $(this).val());
            _this.saveLocal();
            window.location.reload();
        });
        if (lang !== this.config.supportLang[0].id) {
            $.ajax({
                url: 'js/i18n/{0}.json'.format(lang),
                type: 'get',
                dataType: 'json',
                success: function(data) {
                    _this.i18n = data;
                    callback(true);
                },
                error: function(event, xhr, options, exc) {
                    console.log(event, xhr, options, exc);
                    console.log('[ERROR] Failed to load i18n config {0}.json'.format(lang));
                    callback(false);
                }
            })
        } else {
            callback(true);
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
            _this.initSearch()
            _this.initOptions();
            _this.initAppSelector();

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
     */
    setSrcRootPath: function(srcPath) {
        const $selector = $('#app-selector');
        if (!srcPath) srcPath = '';
        const option = $selector.find('option[value="{0}"]'.format(srcPath));
        if (option.length > 0) {
            if ($selector.val() !== srcPath) $selector.val(srcPath);
            $('.app-customRoot-container').hide();
        } else {
            $selector.val('CUSTOM');
            $('.app-customRoot-container').show();
        }
        $('#app-customRoot').val(this.config.srcBasePath + srcPath);
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
            const node = nodes[i];
            if (node[0] === '/') indent = indent.slice(tab.length);
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
        const v = localStorage.getItem('icc-{0}'.format(optionId));
        return v === null ? defaultVal : JSON.parse(v);
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
