# CHANGELOG_20220111

## 1Sheeld
### com.integreight.onesheeld.MainActivity --> com.facebook.FacebookActivity
+ Add tag: isBasicInvocation
(*SheeldsList.launchShieldsOperationActivity()* is a basic invocation)

### com.integreight.onesheeld.MainActivity --> com.integreight.onesheeld.shields.controller.utils.CameraHeadService
+ Add tag: isBasicInvocation
(*SheeldsList.launchShieldsOperationActivity()* is a basic invocation)

### com.integreight.onesheeld.MainActivity --> com.integreight.onesheeld.shields.controller.utils.SpeechRecognitionService
+ Add tag: isBasicInvocation
(*SheeldsList.launchShieldsOperationActivity()* is a basic invocation)

### com.integreight.onesheeld.services.OneSheeldService --> com.integreight.onesheeld.MainActivity
+ Add tag: isBasicInvocation
(*OneSheeldService.showNotification()* is a basic invocation)

## AFWall+

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.AppDetailActivity
+ Add tag: isBasicInvocation
(*MainActivity.filterApps()* is a basic invocation)
+ Add tag: isListenerInvocation
(*TextView.setOnClickListener()* is a listener invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.CustomScriptActivity
+ Add tag: isBasicInvocation
(*MainActivity.setCustomScript()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.HelpActivity
+ Add tag: isBasicInvocation
(*MainActivity.showAbout()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.LogActivity
+ Add tag: isBasicInvocation
(*MainActivity.showLog()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.OldLogActivity
+ Add tag: isBasicInvocation
(*MainActivity.showLog()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.activity.RulesActivity
+ Add tag: isBasicInvocation
(*MainActivity.showRules()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.preferences.PreferencesActivity
+ Add tag: isBasicInvocation
(*MainActivity.showPreferences()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.service.FirewallService
+ Add tag: isBasicInvocation
(*MainActivity.registerNetworkObserver()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.service.RootShellService
+ Add tag: isBasicInvocation
(*MainActivity.startRootShell()* is a basic invocation)

### dev.ukanth.ufirewall.MainActivity --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add tag: isBasicInvocation
(*MainActivity.disableOrEnable()* is a basic invocation)

### dev.ukanth.ufirewall.activity.LogActivity --> dev.ukanth.ufirewall.activity.LogDetailActivity
+ Add tag: isBasicInvocation
(*LogActivity.initializeRecyclerView()* is a basic invocation)
+ Add tag: isListenerInvocation
(*LogRecyclerViewAdapter.recyclerItemClickListener.onItemClick()* is a listener invocation)

### dev.ukanth.ufirewall.plugin.FireReceiver --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add tag: isBasicInvocation
(*Api.setEnabled()* is a basic invocation)

### dev.ukanth.ufirewall.preferences.PreferencesActivity --> dev.ukanth.ufirewall.activity.ProfileActivity
- Remove tag: isLibraryInvocation
(android.preference.Preference is an Android library)
+ Add tag: isListenerInvocation
(*Preference.setOnPreferenceClickListener()* is a listener invocation)

### dev.ukanth.ufirewall.preferences.PreferencesActivity --> dev.ukanth.ufirewall.service.LogService
- Remove tag: isLibraryInvocation
(by filter: RxEvent is an internal library)
+ Add tag: isBasicInvocation
(*PreferencesActivity.subscribe()* is a basic invocation)

### dev.ukanth.ufirewall.preferences.PreferencesActivity --> haibison.android.lockpattern.LockPatternActivity
+ Add tag: isBasicInvocation
(*PreferencesActivity.loadHeadersFromResource()* is a basic invocation)

### dev.ukanth.ufirewall.service.RootShellService --> dev.ukanth.ufirewall.service.RootShellService
+ Add tag: isBasicInvocation
(*RootShellService.reOpenShell()* is a basic invocation)

### dev.ukanth.ufirewall.service.ToggleTileService --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add tag: isBasicInvocation
(*Api.setEnabled()* is a basic invocation)

### dev.ukanth.ufirewall.widget.StatusWidget --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add tag: isBroadcast
(StatusWidget is a static BroadcastReceiver)
+ Add tag: isBasicInvocation
(*Api.setEnabled()* is a basic invocation)

### dev.ukanth.ufirewall.widget.ToggleWidgetActivity --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add tag: isWidget
(StatusWidget is a widget component)
+ Add tag: isBasicInvocation
(*ToggleWidgetActivity.invokeAction()* is a basic invocation)

### dev.ukanth.ufirewall.widget.ToggleWidgetOldActivity --> dev.ukanth.ufirewall.widget.StatusWidget
+ Add listener register lines in call path
+ Add tag: isWidget
(StatusWidget is a widget component)
+ Add tag: isBasicInvocation
(*Api.setEnabled()* is a basic invocation)
+ Add tag: isListenerInvocation
(*Button.setOnClickListener()* is a listener invocation)

## AnkiDroid

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)
+ Add tag: isBasicInvocation
(*CardBrowser.initNavigationDrawer()* is a basic invocation)

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.NoteEditor
+ Add tag: isBasicInvocation
(*CardBrowser.startActivityForResultWithAnimation()* is a basic invocation)

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)
+ Add tag: isBasicInvocation
(*CardBrowser.initNavigationDrawer()* is a basic invocation)

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.Previewer
+ Add tag: isListenerInvocation
(*ListView.setOnItemLongClickListener()* is a listener invocation)
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)

### com.ichi2.anki.CardBrowser --> com.ichi2.anki.Statistics
+ Add tag: isBasicInvocation
(*CardBrowser.initNavigationDrawer()* is a basic invocation)
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.CardTemplateEditor --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.CardTemplateEditor --> com.ichi2.anki.Previewer
+ Add tag: isListenerInvocation
(*ViewPager.addOnPageChangeListener()* is a listener invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.DeckOptions
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)
+ Add tag: isListenerInvocation
(*DeckAdapter.setDeckLongClickListener()* is a listener invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.FilteredDeckOptions
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.MyAccount
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)
+ Add tag: isBasicInvocation
(*DeckPicker.sync()* is a basic invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.NoteEditor
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.getbase.floatingactionbutton.FloatingActionButton**)
+ Add tag: isBasicInvocation
(*DeckPicker.configureFloatingActionsMenu()* is a basic invocation)
+ Add tag: isListenerInvocation
(*FloatingActionButton.setOnClickListener()* is a listener invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)
+ Add tag: isBasicInvocation
(*DeckPicker.initNavigationDrawer()* is a basic invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*DeckPicker.undo()* is a basic invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.Statistics
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.DeckPicker --> com.ichi2.anki.StudyOptionsActivity
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)

### com.ichi2.anki.Info --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.IntentHandler --> com.ichi2.anki.DeckPicker
- Remove unnecessary call paths

### com.ichi2.anki.ModelBrowser --> com.ichi2.anki.CardTemplateEditor
+ Add tag: isLibraryInvocation
(by filter: External library usage **com.afollestad.materialdialogs.MaterialDialog**)

### com.ichi2.anki.ModelBrowser --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.ModelBrowser --> com.ichi2.anki.ModelFieldEditor
+ Fixed missing blank in call path: ".startActivityForResultWithAnimation()->"
(by filter)
+ Add tag: isListenerInvocation
(*ListView.setOnItemClickListener()* is a listener invocation)

### com.ichi2.anki.ModelFieldEditor --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.MyAccount --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.NoteEditor --> com.ichi2.anki.CardTemplateEditor
+ Add tag: isListenerInvocation
(*TextView.setOnClickListener()* is a listener invocation)

### com.ichi2.anki.NoteEditor --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.NoteEditor --> com.ichi2.anki.multimediacard.activity.MultimediaEditFieldActivity
+ Add tag: isBasicInvocation
(*NoteEditor.setNote()* is a basic invocation)
+ Add tag: isListenerInvocation
(*ImageButton.setOnClickListener()* is a listener invocation)

### com.ichi2.anki.Previewer --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Previewer --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.Previewer --> com.ichi2.anki.NoteEditor
+ Add tag: isBasicInvocation
(*AbstractFlashcardViewer.initLayout()* is a basic invocation)

### com.ichi2.anki.Previewer --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Previewer --> com.ichi2.anki.Statistics
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Reviewer --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Reviewer --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.Reviewer --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Reviewer --> com.ichi2.anki.Statistics
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Statistics --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Statistics --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.Statistics --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.Statistics --> com.ichi2.anki.Statistics
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.CardBrowser
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.DeckOptions
+ Add tag: isBasicInvocation
(*StudyOptionsActivity.loadStudyOptionsFragment()* is a basic invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.FilteredDeckOptions
+ Fixed missing blank in call path: "->StudyOptionsFragment.onMenuItemClick()"
(by filter)
+ Add tag: isBasicInvocation
(*StudyOptionsActivity.loadStudyOptionsFragment()* is a basic invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.Preferences
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.Reviewer
+ Add tag: isBasicInvocation
(*StudyOptionsActivity.openReviewer()* is a basic invocation)

### com.ichi2.anki.StudyOptionsActivity --> com.ichi2.anki.Statistics
+ Add tag: isListenerInvocation
(*DrawerLayout.addDrawerListener()* is a listener invocation)

### com.ichi2.anki.multimediacard.activity.MultimediaEditFieldActivity --> com.ichi2.anki.DeckPicker
+ Add tag: isBasicInvocation
(*AnkiActivity.onCollectionLoadError()* is a basic invocation)

### com.ichi2.anki.multimediacard.activity.MultimediaEditFieldActivity --> com.ichi2.anki.multimediacard.activity.LoadPronounciationActivity
+ Add tag: isBasicInvocation
(*MultimediaEditFieldActivity.recreateEditingUi()* is a basic invocation)
+ Add tag: isListenerInvocation
(*Button.setOnClickListener()* is a listener invocation)
