import SwiftUI
import Shared

struct AddEditView: View {
    let itemId: Int64
    @Environment(\.dismiss) var dismiss
    @State private var name = ""
    @State private var expiryDate = Date()
    @State private var quantity = 1.0
    @State private var showError = false

    var body: some View {
        Form {
            Section {
                Button(action: { /* Scan barcode */ }) {
                    Label("Scan Barcode", systemImage: "barcode.viewfinder")
                }
            }

            Section(header: Text("Item Info"), footer: Text(showError ? "Name is required" : "").foregroundColor(.red)) {
                TextField("Name", text: $name)
                DatePicker("Expiry Date", selection: $expiryDate, displayedComponents: .date)
                Stepper("Quantity: \(Int(quantity))", value: $quantity, in: 1...100)
            }

            Button("Save Item") {
                if name.trimmingCharacters(in: .whitespaces).isEmpty {
                    showError = true
                } else {
                    // Save logic
                    dismiss()
                }
            }
        }
        .navigationTitle(itemId == 0 ? "Add Food" : "Edit Food")
    }
}
