package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.view.OutlineViewController;
import com.embervault.adapter.in.ui.viewmodel.OutlineViewModel;

/**
 * Factory that creates the Outline view and its ViewModel.
 */
final class OutlineViewFactory implements ViewFactory {

    @Override
    public ViewCreationResult create(
            ViewPaneDeps deps,
            UUID baseNoteId,
            Consumer<String> onViewSwitch) {
        OutlineViewModel vm = new OutlineViewModel(
                deps.rootNoteTitle(), deps.noteService(),
                deps.noteService(), deps.noteService(),
                deps.noteService(), deps.noteService(),
                deps.noteService(), deps.appState(), deps.eventBus());
        vm.setBaseNoteId(baseNoteId);
        return new ViewCreationResult(
                vm.tabTitleProperty(),
                vm::loadNotes,
                vm::loadNotes,
                c -> {
                    OutlineViewController ctrl =
                            (OutlineViewController) c;
                    ctrl.setOnViewSwitch(onViewSwitch);
                    ctrl.initViewModel(vm);
                },
                vm.selectedNoteIdProperty());
    }
}
