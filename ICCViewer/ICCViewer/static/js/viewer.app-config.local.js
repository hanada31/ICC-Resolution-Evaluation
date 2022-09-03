/**
 * viewer.app-config.js
 */

window.ICCTagViewer = window.ICCTagViewer ? window.ICCTagViewer : {};
$.extend(window.ICCTagViewer, {
    preDefinedApps: [
        { name: '============= Benchmarks =============' },
        { name: 'DroidBench', path: '/benchmarks/DroidBench', xmlUrl: 'labels/bm_DroidBench_oracle.xml' },
        { name: 'ICCBench', path: '/benchmarks/ICCBench', xmlUrl: 'labels/bm_ICCBench_oracle.xml' },
        { name: 'ICCBotBench', path: '/benchmarks/ICCBotBench', xmlUrl: 'labels/bm_ICCBotBench_oracle.xml' },
        { name: 'RAICC', path: '/benchmarks/RAICC', xmlUrl: 'labels/bm_RAICC_oracle.xml' },
        { name: 'storyBoard', path: '/benchmarks/storyBoard', xmlUrl: 'labels/bm_storyBoard_oracle.xml' },

        { name: '========== Open-source Apps ==========' },
        { name: '1Sheeld 1.9.0 (190401)', path: '/1Sheeld/1Sheeld-Android-App-1.9.0', xmlUrl: 'labels/1Sheeld_oracle.xml' },
        { name: 'AFWall+ 3.5.2.1 (20210517)', path: '/AFWall+/afwall-3.1.0/', xmlUrl: 'labels/AFWall+_oracle.xml' },
        { name: 'AnkiDroid 2.14.6 (21406300)', path: '/AnkiDroid/Anki-Android-2.8.4', xmlUrl: 'labels/AnkiDroid_oracle.xml' },
        { name: 'AntennaPod 2.2.1 (2020195)', path: '/AntennaPod/AntennaPod-1.7.1', xmlUrl: 'labels/AntennaPod_oracle.xml' },
        { name: 'Calendula 2.5.11 (42)', path: '/Calendula/calendula-2.5.11', xmlUrl: 'labels/Calendula_oracle.xml' },
        { name: 'TopoSuite 1.2.0 (69)', path: '/ch.hgdev.toposuite/toposuite-android-1.0.3', xmlUrl: 'labels/ch.hgdev.toposuite_oracle.xml' },
        { name: 'TopoSuite 1.2.0 (69) [without public tab]', path: '/ch.hgdev.toposuite/toposuite-android-1.0.3', xmlUrl: 'labels/ch.hgdev.toposuite_oracle_without_public_tab.xml' },
        { name: 'Taskbar 6.1.1 (203)', path: '/com.farmerbb.taskbar/Taskbar-183', xmlUrl: 'labels/com.farmerbb.taskbar_oracle.xml' },
        { name: 'MoneyWallet 4.0.2-floss (55)', path: '/com.oriondev.moneywallet/moneywallet-4.0.2', xmlUrl: 'labels/com.oriondev.moneywallet_oracle.xml' },
        { name: 'Conversations 2.9.13+fcr (42015)', path: '/Conversations/Conversations-2.5.1', xmlUrl: 'labels/Conversations_oracle.xml' },
        { name: 'CSipSimple 0.04-01 (1841)', path: '/CSipSimple/CSipSimple-master', xmlUrl: 'labels/CSipSimple_oracle.xml' },
        { name: 'EteSync 2.2.4 (20204)', path: '/EteSync/android-1.4.6', xmlUrl: 'labels/EteSync_oracle.xml' },
        { name: 'WorkTime 1.1.15 (270)', path: '/eu.vranckaert.worktime_1.1.13.4/eu.vranckaert.worktime_260_src', xmlUrl: 'labels/eu.vranckaert.worktime_oracle.xml' },
        { name: 'Transports Rennes 4.1.3 (413)', path: '/fr.ybo.transportsrennes/TransportsRennes-TR_noGoogleMap_3.8.4', xmlUrl: 'labels/fr.ybo.transportsrennes_oracle.xml' },
        { name: 'iNaturalist 1.22.1 (488)', path: '/iNaturalist/iNaturalistAndroid-1.11.19-343', xmlUrl: 'labels/iNaturalist_oracle.xml' },
        { name: 'K-9 Mail 5.734 (27034)', path: '/K9Mail/k-9-5.600', xmlUrl: 'labels/K9Mail_oracle.xml' },
        { name: 'LinCal 1.3.1 (13)', path: '/Lincal/LinCal-1.3.1', xmlUrl: 'labels/Lincal_oracle.xml' },
        { name: 'Easy Diary 1.4.167 (233)', path: '/me.blog.korn123.easydiary/aaf-easydiary-1.4.153', xmlUrl: 'labels/me.blog.korn123.easydiary_oracle.xml' },
        { name: 'OsmAnd~ 3.9.10 (400)', path: '/net.osmand.plus/OsmAnd-r3.6', xmlUrl: 'labels/net.osmand.plus_oracle.xml' },
        { name: 'Open GPS Tracker 1.3.5 (85)', path: '/OpenGPSTracker/OpenGPSTracker', xmlUrl: 'labels/OpenGPSTracker_oracle.xml' },
        { name: 'OpenKeychain 5.7.5 (57500)', path: '/OpenKeychain/open-keychain-5.2', xmlUrl: 'labels/OpenKeychain_oracle.xml' },
        { name: 'EP Mobile 2.25.4 (69)', path: '/org.epstudios.epmobile/epmobile-2.3.2', xmlUrl: 'labels/org.epstudios.epmobile_oracle.xml' },
        { name: 'AnyMemo 10.9.993 (223)', path: '/org.liberty.android.fantastischmemo/AnyMemo-10.6.3', xmlUrl: 'labels/org.liberty.android.fantastischmemo_oracle.xml' },
        { name: 'Silence 0.16.12-unstable (211)', path: '/org.smssecure.smssecure/Silence-Android-v0.15.1', xmlUrl: 'labels/org.smssecure.smssecure_oracle.xml' },
        { name: 'Tasks 11.10 (111004)', path: '/org.tasks/tasks-4.8.2', xmlUrl: 'labels/org.tasks_oracle.xml' },
        { name: 'Padland 1.8 (23)', path: '/Padland/padland-1.5', xmlUrl: 'labels/Padland_oracle.xml' },
        { name: 'PassAndroid 3.5.7 (357)', path: '/PassAndroid/PassAndroid-3.4.7', xmlUrl: 'labels/PassAndroid_oracle.xml' },
        { name: 'Titan Companion v67-beta (67)', path: '/pt.joaomneto.titancompanion/TitanCompanion-64-beta', xmlUrl: 'labels/pt.joaomneto.titancompanion_oracle.xml' },
        { name: 'Simple Solitaire Collection 3.13 (71)', path: '/Simple-Solitaire/Simple-Solitaire-3.13', xmlUrl: 'labels/Simple-Solitaire_oracle.xml' },
        { name: 'SteamGifts 1.5.13 (1005513)', path: '/SteamGifts/SteamGifts-1.5.11', xmlUrl: 'labels/SteamGifts_oracle.xml' },
        { name: 'Suntimes 0.13.10 (74)', path: '/SuntimesWidget/SuntimesWidget-0.11.3', xmlUrl: 'labels/SuntimesWidget_oracle.xml' },
        { name: 'Syncthing 1.16.0 (4273)', path: '/syncthing/syncthing-android-1.2.0', xmlUrl: 'labels/syncthing_oracle.xml' },
    ]
});