import UIKit

class ActionsViewController: UITableViewController {
    
    fileprivate let CellIdentifier = "CellIdentifier"
    fileprivate let defaultRowHeight = 42
    
    fileprivate var manager: RNTurbolinksManager!
    fileprivate var route: TurbolinksRoute!
    
    convenience init(_ manager: RNTurbolinksManager,_ route: TurbolinksRoute,_ barButtonItem: UIBarButtonItem) {
        self.init()
        self.manager = manager
        self.route = route
        self.tableView.rowHeight = CGFloat(defaultRowHeight)
        self.tableView.isScrollEnabled = false
        self.modalPresentationStyle = .popover;
        self.preferredContentSize.width = 300
        self.preferredContentSize.height = CGFloat(route.actions!.count * defaultRowHeight)
        self.popoverPresentationController?.barButtonItem = barButtonItem
        self.popoverPresentationController?.delegate = self
        self.popoverPresentationController?.backgroundColor = self.view.backgroundColor;
        self.tableView.tableFooterView = UIView(frame: CGRect(x: 0, y: 0, width: tableView.frame.size.width, height: 1))
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.register(UITableViewCell.self, forCellReuseIdentifier: CellIdentifier)
    }
    
    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return route.actions!.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: CellIdentifier, for: indexPath)
        let action = TurbolinksAction(route.actions![indexPath.row])
        cell.textLabel?.text = action.title
        cell.imageView?.image = action.icon
        return cell
    }
    
    override func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let action = TurbolinksAction(route.actions![indexPath.row])
        dismiss(animated: true, completion: { self.manager.handleActionPress(action.id) })
    }
}

extension ActionsViewController: UIPopoverPresentationControllerDelegate {
    func adaptivePresentationStyle(for controller: UIPresentationController, traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        return .none
    }
}

