import SwiftUI
import Shared

struct DashboardView: View {
    @StateObject private var box: ViewModelBox<Shared.DashboardViewModel, Shared.DashboardState>

    init() {
        let vm = KoinKt.getDashboardViewModel()
        _box = StateObject(wrappedValue: ViewModelBox(
            viewModel: vm,
            initialState: vm.state.value,
            observe: vm.observeState
        ))
    }

    var body: some View {
        NavigationStack {
            List {
                Section(header: Text("Summary")) {
                    HStack {
                        SummaryBox(title: "Expired", count: "\(box.state.expiredCount)", color: .red)
                        SummaryBox(title: "3 Days", count: "\(box.state.expiring3DaysCount)", color: .orange)
                        SummaryBox(title: "7 Days", count: "\(box.state.expiring7DaysCount)", color: .yellow)
                    }
                    .padding(.vertical, 4)
                }

                Section(header: Text("Urgent Items")) {
                    if box.state.urgentItems.isEmpty {
                        Text("No urgent items")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(box.state.urgentItems, id: \.id) { item in
                            NavigationLink(destination: AddEditView(itemId: item.id)) {
                                HStack {
                                    Text(item.name)
                                    Spacer()
                                    Text(item.expiryDate.displayString)
                                        .font(.footnote)
                                        .foregroundStyle(.orange)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Stash")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink(destination: AddEditView(itemId: 0)) {
                        Image(systemName: "plus")
                    }
                }
            }
        }
    }
}

struct SummaryBox: View {
    let title: String
    let count: String
    let color: Color

    var body: some View {
        VStack {
            Text(count)
                .font(.title)
                .foregroundColor(color)
            Text(title)
                .font(.caption)
        }
        .frame(maxWidth: .infinity)
    }
}
