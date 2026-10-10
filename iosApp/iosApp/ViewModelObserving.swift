import SwiftUI
import Shared

@MainActor
final class ViewModelBox<VM: AnyObject, State>: ObservableObject {
    @Published var state: State
    let viewModel: VM
    private let closeable: AutoCloseable

    init(
        viewModel: VM,
        initialState: State,
        observe: @escaping (@escaping (State) -> Void) -> AutoCloseable
    ) {
        self.viewModel = viewModel
        self.state = initialState
        self.closeable = observe { [weak self] newState in
            Task { @MainActor [weak self] in
                self?.state = newState
            }
        }
    }

    deinit {
        closeable.close()
    }
}

extension Kotlinx_datetimeLocalDate {
    var displayString: String {
        String(format: "%04d-%02d-%02d", year, monthNumber, dayOfMonth)
    }

    var asDate: Date {
        var components = DateComponents()
        components.year = Int(year)
        components.month = Int(monthNumber)
        components.day = Int(dayOfMonth)
        return Calendar.current.date(from: components) ?? Date()
    }
}

extension Date {
    var asLocalDate: Kotlinx_datetimeLocalDate {
        let components = Calendar.current.dateComponents([.year, .month, .day], from: self)
        return Kotlinx_datetimeKt.LocalDate(
            year: Int32(components.year ?? 2025),
            monthNumber: Int32(components.month ?? 1),
            dayOfMonth: Int32(components.day ?? 1)
        )
    }
}
