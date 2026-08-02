import SwiftUI
import Shared

struct DashboardView: View {
    var body: some View {
        NavigationStack {
            List {
                Section(header: Text("Summary")) {
                    HStack {
                        SummaryBox(title: "Expired", count: "0", color: .red)
                        SummaryBox(title: "3 Days", count: "0", color: .orange)
                        SummaryBox(title: "7 Days", count: "0", color: .yellow)
                    }
                }

                Section(header: Text("Urgent Items")) {
                    Text("No urgent items")
                }
            }
            .navigationTitle("Stash")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
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
