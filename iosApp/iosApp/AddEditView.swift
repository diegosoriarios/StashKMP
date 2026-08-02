import SwiftUI
import Shared

struct AddEditView: View {
    let itemId: Int64
    @State private var name = ""
    @State private var expiryDate = Date()
    @State private var quantity = 1.0

    var body: some View {
        Form {
            Section {
                Button(action: { /* Scan barcode */ }) {
                    Label("Scan Barcode", systemImage: "barcode.viewfinder")
                }
            }

            Section(header: Text("Item Info")) {
                TextField("Name", text: $name)
                DatePicker("Expiry Date", selection: $expiryDate, displayedComponents: .date)
                Stepper("Quantity: \(Int(quantity))", value: $quantity, in: 1...100)
            }

            Button("Save Item") {
                // Save logic
            }
        }
        .navigationTitle(itemId == 0 ? "Add Food" : "Edit Food")
    }
}
