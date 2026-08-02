import SwiftUI
import Shared

struct InventoryView: View {
    @State private var searchText = ""

    var body: some View {
        NavigationStack {
            List {
                Text("Your food inventory will appear here")
            }
            .searchable(text: $searchText)
            .navigationTitle("My Food")
        }
    }
}
