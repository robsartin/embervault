package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.view.TreemapViewController;
import com.embervault.adapter.in.ui.viewmodel.TreemapViewModel;

/**
 * Factory that creates the Treemap view and its ViewModel.
 */
final class TreemapViewFactory implements ViewFactory {

    @Override
    public ViewCreationResult create(
            ViewPaneDeps deps,
            UUID baseNoteId,
            Consumer<String> onViewSwitch) {
        TreemapViewModel vm = new TreemapViewModel(
                deps.rootNoteTitle(), deps.noteService(),
                deps.noteService(), deps.appState());
        vm.setBaseNoteId(baseNoteId);
        return new ViewCreationResult(
                vm.tabTitleProperty(),
                vm::loadNotes,
                vm::loadNotes,
                c -> {
                    TreemapViewController ctrl =
                            (TreemapViewController) c;
                    ctrl.setOnViewSwitch(onViewSwitch);
                    ctrl.initViewModel(vm);
                },
                vm.selectedNoteIdProperty());
    }
}
