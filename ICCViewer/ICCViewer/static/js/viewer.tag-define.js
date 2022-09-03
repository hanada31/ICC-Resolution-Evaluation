"use strict";

window.ICCTagViewer = window.ICCTagViewer ? window.ICCTagViewer : {};
window.ICCTagViewer.config = window.ICCTagViewer.config ? window.ICCTagViewer.config : {};
$.extend(window.ICCTagViewer.config, {
    tags: [
        {
            "id": "entryMethod",
            "name": "Entry Method",
            "subTags": [
                {
                    "id": "isLifeCycle",
                    "name": "LifeCycle",
                    "desc": "The official lifecycle methods provided by Android reference"
                },
                {
                    "id": "isImplicitCallback",
                    "name": "Implicit Callback",
                    "desc": "The callbacks that are registered in Android framework classes or libraries"
                },
                {
                    "id": "isDynamicCallBack",
                    "name": "Dynamic Callback",
                    "desc": "The callbacks that are registered in code of application package"
                },
                {
                    "id": "isStaticCallBack",
                    "name": "Static Callback",
                    "desc": "The callbacks (or their related components) that are declared in the XML files"
                }
            ]
        },
        {
            "id": "exitMethod",
            "name": "Exit Method",
            "subTags": [
                {
                    "id": "isNormalSendICC",
                    "name": "Normal ICC",
                    "desc": "The APIs that normally start a component with objects of type Intent"
                },
                {
                    "id": "isAtypicalSendICC",
                    "name": "Atypical ICC",
                    "desc": "The APIs that atypically start a component with objects of type PendingIntent and/or IntentSender"
                }
            ]
        },
        {
            "id": "intentMatch",
            "name": "Intent Type",
            "subTags": [
                {
                    "id": "isExplicit",
                    "name": "Explicit Intent",
                    "desc": "The Intent whose destination class is given explicitly"
                },
                {
                    "id": "isImplicit",
                    "name": "Implicit Intent",
                    "desc": "The Intent whose destination class is observed by intent-filter matching"
                }
            ]
        },
        {
            "id": "analyzeScope",
            "name": "Analyze Scope",
            "subTags": [
                {
                    "id": "componentScope",
                    "name": "Component Scope",
                    "subTags": [
                        {
                            "id": "isActivity",
                            "name": "Activity",
                            "desc": "The source or destination component is in Activity type"
                        },
                        {
                            "id": "isService",
                            "name": "Service",
                            "desc": "The source or destination component is in Service type"
                        },
                        {
                            "id": "isBroadCast",
                            "name": "Broadcast",
                            "desc": "The source or destination component is in BroadcastReceiver type, and is declared in Android manifest"
                        },
                        {
                            "id": "isDynamicBroadCast",
                            "name": "Dynamic Broadcast",
                            "desc": "The source or destination component is in BroadcastReceiver type, and is registered dynamically"
                        }
                    ]
                },
                {
                    "id": "nonComponentScope",
                    "name": "Non-component Scope",
                    "subTags": [
                        {
                            "id": "isFragment",
                            "name": "Fragment",
                            "desc": "Fragment type is involved in the ICC invocation"
                        },
                        {
                            "id": "isAdapter",
                            "name": "Adapter",
                            "desc": "Adapter type is involved in the ICC invocation"
                        },
                        {
                            "id": "isWidget",
                            "name": "Widget",
                            "desc": "Widget type is involved in the ICC invocation"
                        },
                        {
                            "id": "isOtherClass",
                            "name": "Other Class",
                            "desc": "Other class type is involved in the ICC invocation"
                        }
                    ]
                },
                {
                    "id": "methodScope",
                    "name": "Method Scope",
                    "subTags": [
                        {
                            "id": "isLibraryInvocation",
                            "name": "Library Invocation",
                            "desc": "Library invocation is involved in the ICC invocation"
                        },
                        {
                            "id": "isBasicInvocation",
                            "name": "Simple Invocation",
                            "desc": "Direct invocation to method in application is involved in the ICC invocation"
                        },
                        {
                            "id": "isAsyncInvocation",
                            "name": "Async Invocation",
                            "desc": "Async invocation is involved in the ICC invocation"
                        },
                        {
                            "id": "isListenerInvocation",
                            "name": "Listener Invocation",
                            "desc": "Listener invocation is involved in the ICC invocation"
                        },
                        {
                            "id": "isPolymorphic",
                            "name": "Polymorphic",
                            "desc": "Polymorphic usage is involved in the ICC invocation"
                        }
                    ]
                },
                {
                    "id": "intentFieldScope",
                    "name": "Intent Field Scope",
                    "subTags": [
                        {
                            "id": "isStaticVal",
                            "name": "Static Value",
                            "desc": "The static value is used to observe component for intent"
                        },
                        {
                            "id": "isStringOp",
                            "name": "String Operation",
                            "desc": "The string operation is used to generate extra data for intent"
                        },
                        {
                            "id": "isNoExtra",
                            "name": "No Extra",
                            "desc": "The intent contains no extra data"
                        },
                        {
                            "id": "isContext",
                            "name": "Context-related",
                            "desc": "The value of intent field is context-related"
                        }
                    ]
                }
            ]
        }
    ],
});
