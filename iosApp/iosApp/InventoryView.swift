import SwiftUI
import Shared

struct InventoryView: View {
    @StateObject private var box: ViewModelBox<Shared.InventoryViewModel, Shared.InventoryState>
    @State private var showingAdd = false

    init() {
        let vm = KoinKt.getInventoryViewModel()
        _box = StateObject(wrappedValue: ViewModelBox(
            viewModel: vm,
            initialState: vm.state.value,
            observe: vm.observeState
        ))
    }

    var body: some View {
        NavigationStack {
            Group {
                if box.state.filteredItems.isEmpty {
                    ContentUnavailableView(
                        "No food yet",
                        systemImage: "basket",
                        description: Text("Tap + to add your first item.")
                    )
                } else {
                    List {
                        ForEach(box.state.filteredItems, id: \.id) { item in
                            NavigationLink(destination: AddEditView(itemId: item.id)) {
                                InventoryItemRow(item: item)
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                Button(role: .destructive) {
                                    box.viewModel.discardItem(id: item.id, reason: "Manually removed")
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                                Button {
                                    box.viewModel.consumeItem(id: item.id)
                                } label: {
                                    Label("Use", systemImage: "checkmark")
                                }
                                .tint(.green)
                            }
                            .swipeActions(edge: .leading, allowsFullSwipe: false) {
                                Button {
                                    box.viewModel.reduceQuantity(id: item.id, currentQuantity: item.quantity)
                                } label: {
                                    Label("-1", systemImage: "minus.circle")
                                }
                                .tint(.orange)
                            }
                        }
                    }
                }
            }
            .searchable(text: searchBinding, prompt: "Search food...")
            .navigationTitle("My Food")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showingAdd = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showingAdd) {
                NavigationStack {
                    AddEditView(itemId: 0)
                }
            }
        }
    }

    private var searchBinding: Binding<String> {
        Binding(
            get: { box.state.searchQuery },
            set: { box.viewModel.onSearchQueryChanged(query: $0) }
        )
    }
}

struct InventoryItemRow: View {
    let item: FoodItem

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(item.name)
                .font(.body)
            Text("\(String(format: "%g", item.quantity)) \(item.unit.displayName) · \(item.storageLocation.displayName)")
                .font(.footnote)
                .foregroundStyle(.secondary)
            Text("Expires \(item.expiryDate.displayString)")
                .font(.footnote)
                .foregroundStyle(expiryColor)
        }
        .padding(.vertical, 2)
    }

    private var expiryColor: Color {
        let days = Calendar.current.dateComponents(
            [.day],
            from: Calendar.current.startOfDay(for: Date()),
            to: item.expiryDate.asDate
        ).day ?? 0
        if days < 0 {
            return .red
        }
        if days <= 7 {
            return .orange
        }
        return .secondary
    }
}
