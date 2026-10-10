import SwiftUI
import Shared

struct AddEditView: View {
    let itemId: Int64
    @Environment(\.dismiss) var dismiss
    @StateObject private var box: ViewModelBox<Shared.AddEditViewModel, Shared.AddEditState>
    @State private var expiryDateDraft = Date()
    @State private var showScanner = false

    init(itemId: Int64) {
        self.itemId = itemId
        let vm = KoinKt.getAddEditViewModel()
        _box = StateObject(wrappedValue: ViewModelBox(
            viewModel: vm,
            initialState: vm.state.value,
            observe: vm.observeState
        ))
        if itemId != 0 {
            vm.loadItem(id: itemId)
        }
    }

    private let categories: [Category] = [
        .produce, .dairy, .meat_fish, .frozen, .bakery, .pantry, .beverages, .snacks, .other
    ]

    private let locations: [StorageLocation] = [
        .fridge, .freezer, .pantry, .cabinet, .counter, .other
    ]

    private let units: [FoodUnit] = [
        .units, .g, .kg, .ml, .l, .cups, .slices, .portions
    ]

    var body: some View {
        Form {
            scanSection

            Section(header: Text("Item Info")) {
                TextField("Name", text: nameBinding)
                if let nameError = box.state.nameError {
                    Text(nameError).font(.footnote).foregroundStyle(.red)
                }

                Picker("Category", selection: categoryBinding) {
                    ForEach(categories, id: \.self) { category in
                        Text(category.displayName).tag(category)
                    }
                }

                HStack {
                    TextField("Qty", value: quantityBinding, format: .number)
                        .keyboardType(.decimalPad)
                    Picker("Unit", selection: unitBinding) {
                        ForEach(units, id: \.self) { unit in
                            Text(unit.displayName).tag(unit)
                        }
                    }
                    .pickerStyle(.menu)
                }

                Picker("Location", selection: locationBinding) {
                    ForEach(locations, id: \.self) { location in
                        Text(location.displayName).tag(location)
                    }
                }

                DatePicker("Expiry Date", selection: $expiryDateDraft, displayedComponents: .date)

                Picker("Expiry Type", selection: expiryTypeBinding) {
                    Text("Best before").tag(ExpiryType.best_before)
                    Text("Use by").tag(ExpiryType.use_by)
                }

                TextField("Notes", text: notesBinding, axis: .vertical)
            }

            Button {
                box.viewModel.saveItem()
            } label: {
                if box.state.isSaving {
                    ProgressView()
                } else {
                    Text("Save Item")
                }
            }
        }
        .navigationTitle(itemId == 0 ? "Add Food" : "Edit Food")
        .navigationBarTitleDisplayMode(.inline)
        .interactiveDismissDisabled(box.state.isSaving)
        .sheet(isPresented: $showScanner) {
            if BarcodeScannerView.isSupported {
                BarcodeScannerView { code in
                    showScanner = false
                    box.viewModel.scanBarcode(barcode: code)
                }
                .ignoresSafeArea()
                .overlay(alignment: .bottom) {
                    Text("Align the barcode within the frame")
                        .font(.footnote)
                        .foregroundStyle(.white)
                        .padding(12)
                        .background(.black.opacity(0.6), in: Capsule())
                        .padding(.bottom, 24)
                }
            } else {
                ContentUnavailableView(
                    "Camera unavailable",
                    systemImage: "camera.badge.ellipsis",
                    description: Text("Barcode scanning isn't available on this device.")
                )
            }
        }
        .onChange(of: expiryDateDraft) { _, newDate in
            box.viewModel.onExpiryDateChanged(date: newDate.asLocalDate)
        }
        .onChange(of: box.state.isSaved) { _, saved in
            if saved {
                dismiss()
            }
        }
        .onAppear {
            expiryDateDraft = box.state.expiryDate.asDate
        }
    }

    private var scanSection: some View {
        Section {
            Button {
                showScanner = true
            } label: {
                Label("Scan Barcode", systemImage: "barcode.viewfinder")
            }

            if box.state.isScanning {
                HStack(spacing: 8) {
                    ProgressView()
                    Text("Looking up product…").foregroundStyle(.secondary)
                }
            }

            if !box.state.barcode.isEmpty {
                Text("Barcode: \(box.state.barcode)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }

            if let message = box.state.lookupMessage {
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(box.state.lookupIsError ? Color.red : Color.secondary)
            }
        }
    }

    private var nameBinding: Binding<String> {
        Binding(
            get: { box.state.name },
            set: { box.viewModel.onNameChanged(name: $0) }
        )
    }

    private var categoryBinding: Binding<Category> {
        Binding(
            get: { box.state.category },
            set: { box.viewModel.onCategoryChanged(category: $0) }
        )
    }

    private var quantityBinding: Binding<Double> {
        Binding(
            get: { box.state.quantity },
            set: { box.viewModel.onQuantityChanged(quantity: $0) }
        )
    }

    private var unitBinding: Binding<FoodUnit> {
        Binding(
            get: { box.state.unit },
            set: { box.viewModel.onUnitChanged(unit: $0) }
        )
    }

    private var locationBinding: Binding<StorageLocation> {
        Binding(
            get: { box.state.storageLocation },
            set: { box.viewModel.onLocationChanged(location: $0) }
        )
    }

    private var expiryTypeBinding: Binding<ExpiryType> {
        Binding(
            get: { box.state.expiryType },
            set: { box.viewModel.onExpiryTypeChanged(type: $0) }
        )
    }

    private var notesBinding: Binding<String> {
        Binding(
            get: { box.state.notes },
            set: { box.viewModel.onNotesChanged(notes: $0) }
        )
    }
}
