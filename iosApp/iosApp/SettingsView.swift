import SwiftUI
import Shared

struct SettingsView: View {
    var body: some View {
        NavigationStack {
            List {
                Section(header: Text("Notifications")) {
                    Toggle("Reminders", isOn: .constant(true))
                }

                Section {
                    Button("Clear All Data", role: .destructive) {
                        // Clear logic
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}
