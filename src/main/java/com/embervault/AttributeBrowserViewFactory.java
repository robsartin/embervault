package com.embervault;

import java.util.UUID;
import java.util.function.Consumer;

import com.embervault.adapter.in.ui.view.AttributeBrowserViewController;
import com.embervault.adapter.in.ui.viewmodel.AttributeBrowserViewModel;

/**
 * Factory that creates the Attribute Browser view and its ViewModel.
 */
final class AttributeBrowserViewFactory implements ViewFactory {

  @Override
  public ViewCreationResult create(
      ViewPaneDeps deps,
      UUID baseNoteId,
      Consumer<String> onViewSwitch) {
    AttributeBrowserViewModel vm =
        new AttributeBrowserViewModel(
            deps.noteService(), deps.schemaRegistry());
    vm.setOnDataChanged(deps.refreshAll());
    return new ViewCreationResult(
        vm.tabTitleProperty(),
        vm::groupNotes,
        vm::groupNotes,
        c -> {
          AttributeBrowserViewController ctrl =
              (AttributeBrowserViewController) c;
          ctrl.setOnViewSwitch(onViewSwitch);
          ctrl.initViewModel(vm);
        },
        null);
  }
}
