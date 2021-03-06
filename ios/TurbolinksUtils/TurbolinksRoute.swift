import Turbolinks

class TurbolinksRoute {
    var title: String?
    var subtitle: String?
    var action: Action?
    var url: URL?
    var component: String?
    var modal: Bool?
    var passProps: Dictionary<AnyHashable, Any>?
    var actions: Array<Dictionary<AnyHashable, Any>>?
    var leftButtonIcon: UIImage?
    var tabTitle: String?
    var tabIcon: UIImage?
    
    init(_ route: Dictionary<AnyHashable, Any>) {
        let action = RCTConvert.nsString(route["action"])
        self.title = RCTConvert.nsString(route["title"])
        self.subtitle = RCTConvert.nsString(route["subtitle"])
        self.action = Action.init(rawValue: action ?? "advance")!
        self.url = RCTConvert.nsurl(route["url"])
        self.component = RCTConvert.nsString(route["component"])
        self.modal = RCTConvert.bool(route["modal"]) || false
        self.passProps = RCTConvert.nsDictionary(route["passProps"])
        self.actions = RCTConvert.nsDictionaryArray(route["actions"])
        self.leftButtonIcon = RCTConvert.uiImage(route["leftButtonIcon"])
        self.tabTitle = RCTConvert.nsString(route["tabTitle"])
        self.tabIcon = RCTConvert.uiImage(route["tabIcon"])
    }    
}
