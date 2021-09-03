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
        ignorable: true,
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
        ignorable: true,
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
        ignorable: true,
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
        (flowId, comment) => {
            const tagPath = 'entryMethod.isLifeCycle';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk1 = /[.| ]on(Create|CreateView|ViewCreated|Attach|Detach|Start|Resume|Pause|Stop|Destroy|Receive|StartCommand|Update)\(/.test(comment);
            const chk2 = /[.| ]on(RestoreInstanceState|PostCreate|PostResume|CreateDescription|SaveInstanceState|Bind|Rebind|Unbind|ActivityCreated|ViewStateRestored)\(/.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: sel ^ (chk1 || chk2),
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'entryMethod.isImplicitCallback';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /[.| ]on(OptionsSelected|ActivityResult|RequestPermissionsResult|NavigationItemSelected|AttachedToWindow)\(/.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'entryMethod.isDynamicCallBack';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const patterns = [
                /(add|set|with|reg|regist|register)[^\.\(]*(listener|callback)/i,
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
                extraInfo: ids.join(', ')
            }
        },
        (flowId, comment) => {
            const tagPath = 'entryMethod.isStaticCallBack';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /[ |.]R\./.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: sel ^ chk,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
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
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.componentScope.isActivity';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /Activity\./.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.componentScope.isService';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /Service\./.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
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
                errorTags: [tagPath1, tagPath2]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.componentScope.isDynamicBroadCast';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /registerReceiver\./.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.nonComponentScope.isFragment';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /Fragment/i.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.nonComponentScope.isAdapter';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /Adapter/i.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.nonComponentScope.isWidget';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /Widget/i.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.methodScope.isAsyncInvocation';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /(runOnUiThread|Thread|onPostExecute|AsyncTask| Handler\.)/i.test(comment);
            return {
                autoFix: true,
                ignorable: true,
                error: chk ? !sel : false,
                errorTags: [tagPath]
            }
        },
        (flowId, comment) => {
            const tagPath = 'analyzeScope.methodScope.isPolymorphic';
            const sel = ICCTagViewer.tagSelect(flowId, tagPath);
            const chk = /(extends)/.test(comment);
            return {
                autoFix: true,
                ignorable: false,
                errorTags: [tagPath],
                type: 'Polymorphic usage'
            }
        },
        (flowId, comment) => {
            const tagPath1 = 'exitMethod.isNormalSendICC';
            const tagPath2 = 'exitMethod.isAtypicalSendICC';
            return countEquals(flowId, [tagPath1, tagPath2], 1);
        },
        (flowId, comment) => {
            const tagPath1 = 'intentMatch.isExplicit';
            const tagPath2 = 'analyzeScope.objectScope.isStaticVal';
            return countNoMoreThan(flowId, [tagPath1, tagPath2], 1);
        },
        (flowId, comment) => {
            const tagPath1 = 'analyzeScope.componentScope.isActivity';
            const tagPath2 = 'analyzeScope.componentScope.isService';
            const tagPath3 = 'analyzeScope.componentScope.isBroadCast';
            const tagPath4 = 'analyzeScope.componentScope.isDynamicBroadCast';
            return countNoLessThan(flowId, [tagPath1, tagPath2, tagPath3, tagPath4], 1);
        },
        (flowId, comment) => {
            const tagPath1 = 'intentMatch.isExplicit';
            const tagPath2 = 'intentMatch.isImplicit';
            return countEquals(flowId, [tagPath1, tagPath2], 1);
        },
    ]
});
