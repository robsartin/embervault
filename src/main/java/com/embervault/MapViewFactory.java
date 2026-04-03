package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.view.MapViewController;
import com.embervault.adapter.in.ui.viewmodel.MapViewModel;

/**
 * Factory that creates the Map view and its ViewModel.
 */
final class MapViewFactory implements ViewFactory {

    @Override
    public ViewCreationResult create(
            ViewPaneDeps deps,
            UUID baseNoteId,
            Consumer<String> onViewSwitch) {
        MapViewModel vm = new MapViewModel(
                deps.rootNoteTitle(), deps.noteService(),
                deps.noteService(), deps.noteService(),
                deps.appState(), deps.eventBus());
        vm.setBaseNoteId(baseNoteId);
        return new ViewCreationResult(
                vm.tabTitleProperty(),
                vm::loadNotes,
                vm::loadNotes,
                c -> {
                    MapViewController ctrl =
                            (MapViewController) c;
                    ctrl.setOnViewSwitch(onViewSwitch);
                    ctrl.initViewModel(vm);
                },
                vm.selectedNoteIdProperty());
    }
}
