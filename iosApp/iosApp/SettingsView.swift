import SwiftUI
import Shared

struct SettingsView: View {
    @StateObject private var box: ViewModelBox<Shared.SettingsViewModel, Shared.SettingsState>
    @State private var showClearConfirmation = false

    init() {
        let vm = KoinKt.getSettingsViewModel()
        _box = StateObject(wrappedValue: ViewModelBox(
            viewModel: vm,
            initialState: vm.state.value,
            observe: vm.observeState
        ))
    }

    var body: some View {
        NavigationStack {
            List {
                Section(header: Text("Notifications"), footer: Text("Reminders are delivered at 09:00 on the selected number of days before each item's expiry date.")) {
                    Toggle("Remind 30 days before", isOn: toggleBinding(\.remind30Days) { box.viewModel.toggleRemind30Days() })
                    Toggle("Remind 7 days before", isOn: toggleBinding(\.remind7Days) { box.viewModel.toggleRemind7Days() })
                    Toggle("Remind 1 day before", isOn: toggleBinding(\.remind1Day) { box.viewModel.toggleRemind1Day() })
                }

                Section {
                    Button(role: .destructive) {
                        showClearConfirmation = true
                    } label: {
                        if box.state.isClearing {
                            ProgressView()
                        } else {
                            Text("Clear All Data")
                        }
                    }
                    .disabled(box.state.isClearing)
                }
            }
            .navigationTitle("Settings")
            .confirmationDialog(
                "Clear All Data",
                isPresented: $showClearConfirmation,
                titleVisibility: .visible
            ) {
                Button("Delete all items", role: .destructive) {
                    box.viewModel.clearAllData()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("This permanently removes every item from your stash and cancels all reminders. This cannot be undone.")
            }
        }
    }

    private func toggleBinding(
        _ keyPath: KeyPath<Shared.SettingsState, Bool>,
        onToggle: @escaping () -> Void
    ) -> Binding<Bool> {
        Binding(
            get: { box.state[keyPath: keyPath] },
            set: { _ in onToggle() }
        )
    }
}
