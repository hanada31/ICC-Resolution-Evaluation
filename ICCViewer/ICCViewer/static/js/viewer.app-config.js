/**
 * viewer.app-config.js
 */

 window.ICCTagViewer = window.ICCTagViewer ? window.ICCTagViewer : {};
 $.extend(window.ICCTagViewer, {
     preDefinedApps: [
         {
             "name": "============= Benchmarks ============="
         },
         {
             "name": "DroidBench",
             "pkgName": "bm_DroidBench",
             "apkFile": "bm_DroidBench_apks.zip",
             "srcPackFile": "https://drive.google.com/uc?id=1xYDuhcJtkTqqZAorzbNV9RTwP3lwVMpM&export=download&confirm=t"
         },
         {
             "name": "ICCBench",
             "pkgName": "bm_ICCBench",
             "apkFile": "https://drive.google.com/uc?id=1rFYTQzR7RGFgYkPD4qbZPTIDlEwvuJTA&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1FUjaabaodzzCyz3vRWcpHt1t9PsdJmZ9&export=download&confirm=t"
         },
         {
             "name": "ICCBotBench",
             "pkgName": "bm_ICCBotBench",
             "apkFile": "https://drive.google.com/uc?id=1aHWtxIwnAXaloPQ7fv9qIqMLAcVZVZm7&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1uvNteHYSTiOKD3t5MEMWiW-TYaNc6QE0&export=download&confirm=t"
         },
         {
             "name": "RAICC",
             "pkgName": "bm_RAICC",
             "apkFile": "https://drive.google.com/uc?id=1Ggl1leGPL_pA5julRbGgWIJv2zTCXyjN&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1ISCTe41FdXxtv_2f3bgaoVyqz2V4tDUY&export=download&confirm=t"
         },
         {
             "name": "storyBoard",
             "pkgName": "bm_storyBoard",
             "apkFile": "https://drive.google.com/uc?id=1XzMRWUcUYLdCufRd6uRLoWgRuBOEd4Di&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1_FC5mNeNWaSj9jSGG0o1r6lkoh7X0UMn&export=download&confirm=t"
         },
 
         {
             "name": "========== Open-source Apps =========="
         },
         {
             "name": "1Sheeld 1.9.0 (190401)",
             "pkgName": "com.integreight.onesheeld",
             "apkFile": "https://drive.google.com/uc?id=10jJs2T83i_WeTVrsc273dyAsYOBRQ468&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1wJBih8CKtZZt_VDWsRTlhigcTfdjGFgR&export=download&confirm=t"
         },
         {
             "name": "AFWall+ 3.1.0 (17111)",
             "pkgName": "dev.ukanth.ufirewall",
             "apkFile": "https://drive.google.com/uc?id=1e7Rdptuj3eKaxl_g7a2g_M66XcnnCIvH&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1Q2LXWLEXDfeHpKfCWbpr_HWQUppS6O7g&export=download&confirm=t"
         },
         {
             "name": "AnkiDroid 2.8.4 (20804300)",
             "pkgName": "com.ichi2.anki",
             "apkFile": "https://drive.google.com/uc?id=1rwtqg_gYGvjgp5xKW0TvVspXjjRmJpvZ&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1lVjhIo85VU_25KEPAqwRXxJbtfIUUztZ&export=download&confirm=t"
         },
         {
             "name": "AntennaPod 1.7.1 (1070195)",
             "pkgName": "de.danoeh.antennapod",
             "apkFile": "https://drive.google.com/uc?id=1wAoFUfSmXT2zXv_bKFGOgRxkxpo4IwPf&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1dRCIU-wxOg4E5CGTuIqtP3UseH649DA5&export=download&confirm=t"
         },
         {
             "name": "AnyMemo 10.6.3 (211)",
             "pkgName": "org.liberty.android.fantastischmemo",
             "apkFile": "https://drive.google.com/uc?id=14JktVP6Gl9Bj7TM7Bvtrc9mBaL6sLzTT&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1xu2qAaxRCg2LjGz-gx2gAU-oJo1nhHsr&export=download&confirm=t"
         },
         {
             "name": "Calendula 2.5.11 (42)",
             "pkgName": "es.usc.citius.servando.calendula",
             "apkFile": "https://drive.google.com/uc?id=16i6JvNPlT5UTZPxyYsuRS3wj6oVlLX3O&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1XnaJI4nnBzdYpTfiHpH33jvRXt2R81RG&export=download&confirm=t"
         },
         {
             "name": "Conversations 2.5.1+pcr (327)",
             "pkgName": "eu.siacs.conversations",
             "apkFile": "https://drive.google.com/uc?id=1P0kdXqZOZWS72US8WNXZ8lko_hhC2WZa&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1MQeIT85kUNHTOHzQv9kA0ntrIsfyvjSj&export=download&confirm=t"
         },
         {
             "name": "CSipSimple 0.04-01 (1841)",
             "pkgName": "com.csipsimple",
             "apkFile": "https://drive.google.com/uc?id=1R5U9b3HyppEq8zqN3bDo6kL2y0quENX8&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=10_FZ3dYslkFLGEMR0e6HBtwDHZz8MKxG&export=download&confirm=t"
         },
         {
             "name": "Easy Diary 1.4.153 (219)",
             "pkgName": "me.blog.korn123.easydiary",
             "apkFile": "https://drive.google.com/uc?id=1Xd8U6VuWGxkg7TsxH_voB97CWCQJOM30&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=122f8kYq1n1BhYCagrtRNgBCwmqXAvRTI&export=download&confirm=t"
         },
         {
             "name": "EP Mobile 2.3.2 (33)",
             "pkgName": "org.epstudios.epmobile",
             "apkFile": "https://drive.google.com/uc?id=1lSHPHhtpAtw1rqiBbpACWyEmf6wWRDR4&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=129JHlxNTrCivc19Z5ZpjRD4dQziaQKP9&export=download&confirm=t"
         },
         {
             "name": "EteSync 1.4.6 (71)",
             "pkgName": "com.etesync.syncadapter",
             "apkFile": "https://drive.google.com/uc?id=1tRlJMjjqDWedQCElZjXVYfPG6N6XOhyB&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1Ikn7SBCAUWAeUp4QweSY2TFfuBFW4zj1&export=download&confirm=t"
         },
         {
             "name": "iNaturalist 1.11.19 (343)",
             "pkgName": "org.inaturalist.android",
             "apkFile": "https://drive.google.com/uc?id=1QGZPqC9EF32Qj_WYZyIQhhFRlGOkDel7&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1UcCYjCfnoavqZ5Nv5yPGVHjwd9cY96Cw&export=download&confirm=t"
         },
         {
             "name": "K-9 Mail 5.600 (26000)",
             "pkgName": "com.fsck.k9",
             "apkFile": "https://drive.google.com/uc?id=1sCtfjE0ms22OM0n0DhfhGoKPVr7T1Upo&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1uIB2l3is5fBfrKdOw2yYjhLW3Xhjdw8n&export=download&confirm=t"
         },
         {
             "name": "LinCal 1.3.1 (13)",
             "pkgName": "felixwiemuth.lincal",
             "apkFile": "https://drive.google.com/uc?id=11mmHkDHKRWKKysDoujzia8ePg5MjrCxG&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1FT8s_lt2LOftCDUVqjBoNbNp4cjpEMVE&export=download&confirm=t"
         },
         {
             "name": "MoneyWallet 4.0.2-floss (55)",
             "pkgName": "com.oriondev.moneywallet",
             "apkFile": "https://drive.google.com/uc?id=1C5SvIKGGbEJ-ZMp8QDxh5YdcH6N2sjre&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1nzcvttbmSyhGN1UMZOqadeKDu20czidO&export=download&confirm=t"
         },
         {
             "name": "Open GPS Tracker 1.3.5 (85)",
             "pkgName": "nl.sogeti.android.gpstracker",
             "apkFile": "https://drive.google.com/uc?id=1gJjkQY-gAWGjZ4AHThE46CkPnMnt1s3M&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1h3EaKmnhqApe7-PfvE-7otiJjE-1SERW&export=download&confirm=t"
         },
         {
             "name": "OpenKeychain 5.2 (52009)",
             "pkgName": "org.sufficientlysecure.keychain",
             "apkFile": "https://drive.google.com/uc?id=1XJDUk5KQY3oxZUXLYg258SbgzodkG_vV&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1Rfa1edh6FAZKNLiJvNqnhgXJRdWH2jlc&export=download&confirm=t"
         },
         {
             "name": "OsmAnd~ 3.5.2 (352)",
             "pkgName": "net.osmand.plus",
             "apkFile": "https://drive.google.com/uc?id=1CpxD_1Mb0h_vRdZnAPI9ldx8iXyb6Py6&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1v3p9ApN5u6P7_H1rduRillyucXn2iXW1&export=download&confirm=t"
         },
         {
             "name": "Padland 1.5 (20)",
             "pkgName": "com.mikifus.padland",
             "apkFile": "https://drive.google.com/uc?id=1mDo08_mKtaKUWs2TeNTnlJGqzgU8wbrt&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1YnFL3lbFM050acQt9le8271gil0ZYW-d&export=download&confirm=t"
         },
         {
             "name": "PassAndroid 3.4.7 (347)",
             "pkgName": "org.ligi.passandroid",
             "apkFile": "https://drive.google.com/uc?id=11gLUG9uhBQdafwAT5TVRgEMScBIPFECH&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1HskDcgd9BE_QwZA92GNWceyaw9XguwrH&export=download&confirm=t"
         },
         {
             "name": "Silence 0.15.1 (129)",
             "pkgName": "org.smssecure.smssecure",
             "apkFile": "https://drive.google.com/uc?id=1oGudW-UjNHeVOXW7utYv4_OOyjg9s-na&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1Y-mj-B02-KSFFZ8qbszGmjgAc3BLJ0dJ&export=download&confirm=t"
         },
         {
             "name": "Simple Solitaire Collection 3.13 (71)",
             "pkgName": "de.tobiasbielefeld.solitaire",
             "apkFile": "https://drive.google.com/uc?id=1Cc2fwDhKxhlOmm4AEh06plPHpTzq91Lp&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1SybeFDs6z4jPZxpSLy_0T8QtrJDVk6es&export=download&confirm=t"
         },
         {
             "name": "SteamGifts 1.5.11 (1005511)",
             "pkgName": "net.mabako.steamgifts",
             "apkFile": "https://drive.google.com/uc?id=1gsd3z3vxZYHfNG9BVuRuasjWOpH3pMHO&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1PlAKEK_brm0hbaaE65UDiKJEJPyyW13T&export=download&confirm=t"
         },
         {
             "name": "Suntimes 0.11.3 (44)",
             "pkgName": "com.forrestguice.suntimeswidget",
             "apkFile": "https://drive.google.com/uc?id=172rjzfDldIcYLKUK_mEC4Hthe6JfJmyl&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=19Me0J7ipEEltAMn-NoinbMRT6f5lDyCT&export=download&confirm=t"
         },
         {
             "name": "Syncthing 1.2.0 (4180)",
             "pkgName": "com.nutomic.syncthingandroid",
             "apkFile": "https://drive.google.com/uc?id=1Mv2nu2zYF4OU9ZlZydvSWEgQkfaaIb0a&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=14d_-JCR6eL4qsyq1B3LQzM4-q8b6IAl3&export=download&confirm=t"
         },
         {
             "name": "Taskbar 3.1 (123)",
             "pkgName": "com.farmerbb.taskbar",
             "apkFile": "https://drive.google.com/uc?id=1uaF2oXKRiDsZycUfYZEMOSSXB_ufqDHR&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=11_MSJxfd8gaN_IvVJuKseS1A0erzBxUa&export=download&confirm=t"
         },
         {
             "name": "Tasks 4.8.2 (383)",
             "pkgName": "org.tasks",
             "apkFile": "https://drive.google.com/uc?id=1xVF6zr-DQ2S4Om53qBX7_t106oe2y-ZU&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1ApGhLaruX1UFZA-c21heQUz8jHX_Qkz3&export=download&confirm=t"
         },
         {
             "name": "Titan Companion v63-beta (63)",
             "pkgName": "pt.joaomneto.titancompanion",
             "apkFile": "https://drive.google.com/uc?id=1NS-uCm32jiDPKwO26bxEpQsR8xZjdshM&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1MdJgZYyL4XrKnQFuDxK9SxN6kcHkkc4-&export=download&confirm=t"
         },
         {
             "name": "TopoSuite 1.0.3 (56)",
             "pkgName": "ch.hgdev.toposuite",
             "apkFile": "https://drive.google.com/uc?id=1Qt0pYQq39150D3-6PRrxjRwHU8FC7isK&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1sto0HIJn0-0_r_2PpVfeZrjo1x_FastW&export=download&confirm=t"
         },
         {
             "name": "TopoSuite 1.0.3 (56) [without public tab]",
             "pkgName": "ch.hgdev.toposuite",
             "oracleFile": "ch.hgdev.toposuite_oracle_without_public_tab.xml",
             "apkFile": "https://drive.google.com/uc?id=1Qt0pYQq39150D3-6PRrxjRwHU8FC7isK&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1sto0HIJn0-0_r_2PpVfeZrjo1x_FastW&export=download&confirm=t"
         },
         {
             "name": "Transports Rennes 4.1.3 (413)",
             "pkgName": "fr.ybo.transportsrennes",
             "apkFile": "https://drive.google.com/uc?id=1RXjgPPruT9QK0QcQDti8LuOJQU7bPA5y&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1gB52YQjJdbEaJJN4d5ue8AHjxEt8bSvW&export=download&confirm=t"
         },
         {
             "name": "WorkTime 1.1.15 (270)",
             "pkgName": "eu.vranckaert.worktime",
             "apkFile": "https://drive.google.com/uc?id=1o8_DYqFLX6G1mjJze72ZwnWQdcJXS8xd&export=download&confirm=t",
             "srcPackFile": "https://drive.google.com/uc?id=1ZDX2XwkzbfWxUSNigDnCIGmgSq16Kl7W&export=download&confirm=t"
         }
     ]
 });