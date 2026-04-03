package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.view.HyperbolicViewController;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicLayoutStrategy;
import com.embervault.adapter.in.ui.viewmodel.HyperbolicViewModel;

/**
 * Factory that creates the Hyperbolic view and its ViewModel.
 */
final class HyperbolicViewFactory implements ViewFactory {

    @Override
    public ViewCreationResult create(
            ViewPaneDeps deps,
            UUID baseNoteId,
            Consumer<String> onViewSwitch) {
        HyperbolicViewModel vm = new HyperbolicViewModel(
                deps.noteService(), deps.linkService(),
                deps.appState(), deps.eventBus(),
                new HyperbolicLayoutStrategy());
        if (baseNoteId != null) {
            vm.setFocusNote(baseNoteId);
        }
        return new ViewCreationResult(
                vm.tabTitleProperty(),
                () -> {
                    if (vm.getFocusNoteId() != null) {
                        vm.setFocusNote(
                                vm.getFocusNoteId());
                    }
                },
                () -> { },
                c -> {
                    HyperbolicViewController ctrl =
                            (HyperbolicViewController) c;
                    ctrl.setOnViewSwitch(onViewSwitch);
                    ctrl.initViewModel(vm);
                },
                vm.selectedNoteIdProperty());
    }
}
