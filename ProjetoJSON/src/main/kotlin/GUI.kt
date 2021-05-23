import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*
import javax.swing.JButton

interface FrameView {
    fun textToImage(shell:Shell, obj: JsonElements): Image?
    fun modifiedText(obj: JsonElements): String
    fun excludeBooleans(obj: JsonValue): Boolean
}

interface Action {
    val name: String
    fun execute(gui: JsonTree)
}

class JsonTree() {
    val shell: Shell
    var tree: Tree

    @Inject
    private lateinit var setup: FrameView
    @InjectAdd
    private val actions = mutableListOf<Action>()

    val list = mutableListOf<TreeItem>()
    //Funções para encontrar subItems
    fun getAllTreeSubItems(item: TreeItem, allItems: MutableList<TreeItem>){
        var children = item.items
        for(i in children) {
            allItems.add(i)
            getAllTreeSubItems(i,allItems)
        }
    }

    //Funções para encontrar items
    fun getAllTreeItems(tree: Tree, childrenList: MutableList<TreeItem>){
        for(i in tree.items){
            childrenList.add(i)
            getAllTreeSubItems(i, childrenList)
        }
    }

    init {
        shell = Shell(Display.getDefault())
        shell.setSize(250, 200)
        shell.text = "JSON Display"
        shell.layout = GridLayout(2,true)


        tree = Tree(shell, SWT.SINGLE or SWT.BORDER)

        val label = Label(shell, SWT.NONE)
        val labelLayout = GridData()
        labelLayout.widthHint= 250
        labelLayout.heightHint = 400
        label.setLayoutData(labelLayout)

        tree.addSelectionListener(object : SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                label.text = tree.selection.first().data as String?
            }
        })

        val searchText = Text(shell, SWT.BOTTOM)
        searchText.addModifyListener {
            getAllTreeItems(tree, list)
            list.forEach {
                if (searchText.text == it.text) {
                    it.background = Color(255, 255, 0)
                } else {
                    it.background = Color(255, 255, 255)
                }
            }
        }

    }

    fun open(root : JSON) {
        var dropDown :TreeItem? = null
        root.accept(object : Visitor {

            override fun visit(jsonObject: JsonObject) {
                if (dropDown == null) {
                    dropDown = TreeItem(tree, SWT.NONE)
                    dropDown!!.text = setup.modifiedText(jsonObject)
                    dropDown!!.image = setup.textToImage(shell,jsonObject)
                    dropDown!!.data = jsonObject.generate()
                } else {
                    val item = TreeItem(dropDown, SWT.NONE)
                    item.text = setup.modifiedText(jsonObject)
                    item.image = setup.textToImage(shell,jsonObject)
                    item.data = jsonObject.generate()
                    dropDown = item
                }
            }

            override fun endVisit(jsonObject: JsonObject) {
                if (dropDown!!.parentItem == null){
                    dropDown = null
                } else {
                    dropDown = dropDown!!.parentItem
                }
            }

            override fun visit(jsonArray: JsonArray) {
                val item = TreeItem(dropDown, SWT.NONE)
                item.text = setup.modifiedText(jsonArray)
                item.image = setup.textToImage(shell,jsonArray)
                item.data = jsonArray.generate()
                dropDown = item
            }

            override fun endVisit(jsonArray: JsonArray) {
                //Nunca vai ser o primeiro
                    dropDown = dropDown!!.parentItem
            }


            override fun visit(jsonValue: JsonValue) {
                if (jsonValue.value is JsonNumber || jsonValue.value is JsonString || jsonValue.value is JsonBoolean) {
                    val item = TreeItem(dropDown, SWT.NONE)
                    item.text = jsonValue.value.generate() as String?
                    item.image = setup.textToImage(shell,jsonValue)
                    item.data = jsonValue.value.generate() as String? + "\n"
                    if (setup.excludeBooleans(jsonValue)){
                        item.dispose()
                    }
                }
            }


        })

        actions.forEach { action ->
            val button = Button(shell,SWT.PUSH)
            button.text = action.name
            button.addSelectionListener(object: SelectionAdapter() {
                override fun widgetSelected(e: SelectionEvent) {
                    action.execute(this@JsonTree)
                }
            })
        }

        tree.expandAll()
        shell.pack()
        shell.open()
        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        display.dispose()
    }

    // auxiliares para varrer a árvore

    fun Tree.expandAll() = traverse { it.expanded = true }

    fun Tree.traverse(visitor: (TreeItem) -> Unit) {
        fun TreeItem.traverse() {
            visitor(this)
            items.forEach {
                it.traverse()
            }
        }
        items.forEach { it.traverse() }
    }


}

