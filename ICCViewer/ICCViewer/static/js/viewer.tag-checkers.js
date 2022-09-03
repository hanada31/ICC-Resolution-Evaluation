/**
 * viewer.tag-checkers.js
 */

const numTxt = ['zero', 'one', 'two', 'three', 'four', 'five', 'six', 'seven', 'eight', 'nine'];

function countSel(flowId, tags) {
    let sum = 0;
    tags.forEach(function(tag) {
        const t = ICCTagViewer.tagSelect(flowId, tag);
        if (t) sum++;
    });
    return sum;
}

function countEquals(flowId, tags, num) {
    const chk = countSel(flowId, tags) === num;
    return {
        autoFix: false,
        ignorable: false,
        error: !chk,
        errorTags: tags,
        msgPrefix: ICCTagViewer._T('Can only select {0} of these labels').format(
            num < 10 ? ICCTagViewer._T(numTxt[num]) : num
        ),
        msgSuffix: ''
    }
}

function countNoMoreThan(flowId, tags, num) {
    const chk = countSel(flowId, tags) <= num;
    return {
        autoFix: false,
        ignorable: false,
        error: !chk,
        errorTags: tags,
        msgPrefix: ICCTagViewer._T('Cannot select more than {0} of these labels').format(
            num < 10 ? ICCTagViewer._T(numTxt[num]) : num
        ),
        msgSuffix: ''
    }
}

function countNoLessThan(flowId, tags, num) {
    const chk = countSel(flowId, tags) >= num;
    return {
        autoFix: false,
        ignorable: false,
        error: !chk,
        errorTags: tags,
        msgPrefix: ICCTagViewer._T('Cannot select less than {0} of these labels').format(
            num < 10 ? ICCTagViewer._T(numTxt[num]) : num
        ),
        msgSuffix: ''
    }
}

window.ICCTagViewer = window.ICCTagViewer ? window.ICCTagViewer : {};
$.extend(window.ICCTagViewer, {
    checkers: [
        {
            id: 'isLifeCycle',
            name: "Checker of lifecycle related invocation",
            desc: "Whether common life cycle method name appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'entryMethod.isLifeCycle';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk1 = /[.| ]on(Create|CreateView|ViewCreated|Attach|Detach|Start|Resume|Pause|Stop|Destroy|Receive|StartCommand|Update)\(/.test(comment);
                const chk2 = /[.| ]on(RestoreInstanceState|PostCreate|PostResume|CreateDescription|SaveInstanceState|Bind|Rebind|Unbind|ActivityCreated|ViewStateRestored)\(/.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: sel ^ (chk1 || chk2),
                    errorTags: [tagPath],
                    type: 'isLifeCycle',
                }
            }
        },
        {
            id: 'isImplicitCallback',
            name: "Checker of implicit callback",
            desc: "Whether common implicit callback method name appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'entryMethod.isImplicitCallback';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /[.| ]on(OptionsSelected|ActivityResult|RequestPermissionsResult|NavigationItemSelected|AttachedToWindow)\(/.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isImplicitCallback',
                }
            }
        },
        {
            id: 'isDynamicCallBack',
            name: "Checker of dynamic callback",
            desc: "Whether common dynamic callback method name appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'entryMethod.isDynamicCallBack';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const patterns = [
                    /(add|set|with|reg|regist|register)[^.(]*(listener|callback)/i,
                    /listener.on/i,
                    /[.| ]onPositive\(/i,
                    /[.| ]set(Positive|Negative)Button\(/i
                ];
                let ids = [];
                for (let i = 0; i < patterns.length; i++) {
                    if (patterns[i].test(comment)) ids.push(i);
                }
                const chk = ids.length > 0;
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    extraInfo: ids.join(', '),
                    type: 'isDynamicCallBack',
                }
            }
        },
        {
            id: 'isStaticCallBack',
            name: "Checker of static callback",
            desc: "Whether static resource reference (R.) appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'entryMethod.isStaticCallBack';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /[ |*]R\./.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: sel ^ chk,
                    errorTags: [tagPath],
                    type: 'isStaticCallBack',
                }
            }
        },
        {
            id: 'isNormalSendICC',
            name: "Checker of normal ICC",
            desc: "Whether typical ICC method name appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'exitMethod.isNormalSendICC';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const patterns = [
                    /[.| ]startActivity(ForResult|FromChild|FromFragment|IfNeeded)*\(/,
                    /[.| ]start(Activities|Service|ForegroundService)\(/i,
                    /[.| ]send(Ordered)*Broadcast(AsUser|WithMultiplePermissions)*\(/i,
                    /[.| ](bind|stop|unbind)Service\(/i
                ];
                let ids = [];
                for (let i = 0; i < patterns.length; i++) {
                    if (patterns[i].test(comment)) ids.push(i);
                }
                const chk = ids.length > 0;
                return {
                    autoFix: true,
                    ignorable: true,
                    error: sel ^ chk,
                    errorTags: [tagPath],
                    type: 'isNormalSendICC',
                }
            }
        },
        {
            id: 'isActivity',
            name: "Checker of Activity class",
            desc: "Whether the keyword \"Activity\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.componentScope.isActivity';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /Activity\./.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isActivity',
                }
            }
        },
        {
            id: 'isService',
            name: "Checker of Service class",
            desc: "Whether the keyword \"Service\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.componentScope.isService';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /Service\./.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isService',
                }
            }
        },
        {
            id: 'isBroadcast',
            name: "Checker of Receiver class",
            desc: "Whether the keyword \"Receiver\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath1 = 'analyzeScope.componentScope.isBroadCast';
                const tagPath2 = 'analyzeScope.componentScope.isDynamicBroadCast';
                const t1 = ICCTagViewer.tagSelect(flowId, tagPath1);
                const t2 = ICCTagViewer.tagSelect(flowId, tagPath2);
                const sel = t1 || t2;
                const chk = /Receiver\./.test(comment);
                return {
                    autoFix: false,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath1, tagPath2],
                    type: 'isBroadcast',
                }
            }
        },
        {
            id: 'isDynamicBroadcast',
            name: "Checker of dynamic broadcast",
            desc: "Whether the keyword \"registerReceiver\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.componentScope.isDynamicBroadCast';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /registerReceiver\./.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isDynamicBroadcast',
                }
            }
        },
        {
            id: 'isFragment',
            name: "Checker of Fragment invocation",
            desc: "Whether the keyword \"Fragment\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.nonComponentScope.isFragment';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /Fragment/i.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isFragment',
                }
            }
        },
        {
            id: 'isAdapter',
            name: "Checker of Adapter invocation",
            desc: "Whether the keyword \"Adapter\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.nonComponentScope.isAdapter';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /Adapter/i.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isAdapter',
                }
            }
        },
        {
            id: 'isWidget',
            name: "Checker of Widget invocation",
            desc: "Whether the keyword \"Widget\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.nonComponentScope.isWidget';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /Widget/i.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isWidget',
                }
            }
        },
        {
            id: 'isAsyncInvocation',
            name: "Checker of async invocation",
            desc: "Whether any keyword related to async invoke appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.methodScope.isAsyncInvocation';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /(runOnUiThread|Thread|onPostExecute|AsyncTask| Handler\.)/i.test(comment);
                return {
                    autoFix: true,
                    ignorable: true,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isAsyncInvocation',
                }
            }
        },
        {
            id: 'isPolymorphic',
            name: "Checker of polymorphism",
            desc: "Whether the keyword \"extends\" appears in the call path",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.methodScope.isPolymorphic';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const chk = /(extends)/.test(comment);
                return {
                    autoFix: true,
                    ignorable: false,
                    error: chk ? !sel : false,
                    errorTags: [tagPath],
                    type: 'isPolymorphic'
                }
            }
        },
        {
            id: 'isStaticVal',
            name: "Checker of static value (mutual exclusion)",
            desc: "Cannot select more than one of the labels \"isExplicit\" and \"isStaticVal\"",
            func: (flowId, comment) => {
                const tagPath1 = 'intentMatch.isExplicit';
                const tagPath2 = 'analyzeScope.intentFieldScope.isStaticVal';
                return countNoMoreThan(flowId, [tagPath1, tagPath2], 1);
            }
        },
        {
            id: 'isExitMethodSelected',
            name: "Checker of exit method (mutual exclusion)",
            desc: "Can only select one of the labels \"isNormalSendICC\" and \"isAtypicalSendICC\"",
            func: (flowId, comment) => {
                const tagPath1 = 'exitMethod.isNormalSendICC';
                const tagPath2 = 'exitMethod.isAtypicalSendICC';
                return countEquals(flowId, [tagPath1, tagPath2], 1);
            }
        },
        {
            id: 'isComponentTypeSelected',
            name: "Checker of component type (mutual exclusion)",
            desc: "Cannot select less than one of the labels \"isActivity\", \"isService\", \"isBroadCast\" and \"isDynamicBroadCast\"",
            func: (flowId, comment) => {
                const tagPath1 = 'analyzeScope.componentScope.isActivity';
                const tagPath2 = 'analyzeScope.componentScope.isService';
                const tagPath3 = 'analyzeScope.componentScope.isBroadCast';
                const tagPath4 = 'analyzeScope.componentScope.isDynamicBroadCast';
                return countNoLessThan(flowId, [tagPath1, tagPath2, tagPath3, tagPath4], 1);
            }
        },
        {
            id: 'isIntentTypeSelected',
            name: "Checker of Intent type (mutual exclusion)",
            desc: "Can only select one of the labels \"isExplicit\" and \"isImplicit\"",
            func: (flowId, comment) => {
                const tagPath1 = 'intentMatch.isExplicit';
                const tagPath2 = 'intentMatch.isImplicit';
                return countEquals(flowId, [tagPath1, tagPath2], 1);
            }
        },
        {
            id: 'isNoExtra',
            name: "Checker of no extra",
            desc: "Whether extra appears in intent fields",
            func: (flowId, comment) => {
                const tagPath = 'analyzeScope.intentFieldScope.isNoExtra';
                const sel = ICCTagViewer.tagSelect(flowId, tagPath);
                const ifLines = $('#icc-flow-{0} .intent-field-line'.format(flowId));
                let hasExtraFields = false;
                ifLines.toArray().some(function(elem, i){
                    const $elem = $(elem);
                    const ifType = $elem.find('.intent-field-sel').val().trim();
                    const ifVal = $elem.find('.intent-field-val').val().trim();
                    if (ifType === 'extra' && ifVal !== '') {
                        hasExtraFields = true;
                        return true;
                    }
                });
                return {
                    autoFix: false,
                    ignorable: false,
                    error: sel === hasExtraFields,
                    errorTags: [tagPath],
                    type: 'isNoExtra'
                }
            }
        },

        // {
        //     id: 'isListenerInvocation',
        //     name: "listener invocation",
        //     desc: "Whether any keyword related to listener invocation appears in the call path",
        //     isPublic: false,
        //     func: (flowId, comment) => {
        //         const tagPath = 'analyzeScope.methodScope.isListenerInvocation';
        //         const sel = ICCTagViewer.tagSelect(flowId, tagPath);
        //         const chk = /(add|set|with|reg|regist|register)[^.(]*(listener)/i.test(comment);
        //         return {
        //             autoFix: true,
        //             ignorable: true,
        //             error: chk ? !sel : false,
        //             errorTags: [tagPath],
        //             type: 'isListenerInvocation',
        //         }
        //     }
        // },
        // {
        //     id: 'isImplicitAndroidManifest',
        //     name: "implicit Intent",
        //     desc: "Whether the keyword \"AndroidManifest.xml\" appears in the call lines",
        //     isPublic: false,
        //     func: (flowId, comment) => {
        //         const tagPath = 'intentMatch.isImplicit';
        //         const sel = ICCTagViewer.tagSelect(flowId, tagPath);
        //         let chk = false;
        //         const callLines = $('#icc-flow-{0}'.format(flowId)).find('.call-line');
        //         callLines.toArray().some(function(elem, i){
        //             const fp = $(elem).find('input').val();
        //             if (/(AndroidManifest.xml)/i.test(fp)) {
        //                 chk = true;
        //                 return true;
        //             }
        //         });
        //         return {
        //             autoFix: false,
        //             ignorable: true,
        //             msgPrefix: ICCTagViewer._T('Please check whether IntentFilter is defined in AndroidManifest.xml or not when tag'),
        //             msgSuffix: ICCTagViewer._T('has been selected.'),
        //             error: sel ? !chk : false,
        //             errorTags: [tagPath],
        //             type: 'isImplicitAndroidManifest'
        //         }
        //     }
        // },
    ]
});
