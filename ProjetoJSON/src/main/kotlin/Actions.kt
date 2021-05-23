import org.eclipse.swt.SWT
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.*
import org.eclipse.swt.widgets.*
import java.io.File
import java.io.FileWriter


class DefaultView : FrameView{

    override fun modifiedText(obj: JsonElements): String {
        if (obj is JsonObject)
            return "Objeto"
        else if(obj is JsonArray)
            return "Array"
        return "Undefined"
    }

    override fun excludeBooleans(obj: JsonValue):Boolean {
        return false
    }

    override fun textToImage(shell: Shell, obj: JsonElements): Image? {
        return null
    }

}

class ImageView : FrameView{

    override fun textToImage(shell:Shell, obj: JsonElements): Image? {
        var icon: Image? = null
        if(obj is JsonObject || obj is JsonArray) {
            icon = Image(shell.display, "C:\\Users\\Asus\\IdeaProjects\\ProjetoJSON\\folder.jpg")
            return icon
        } else if (obj is JsonValue) {
            icon = Image(shell.display, "C:\\Users\\Asus\\IdeaProjects\\ProjetoJSON\\file.jpg")
            return icon
        }
        return icon
    }

    override fun modifiedText(obj: JsonElements): String {
        if (obj is JsonObject) {
            val x = obj.getList().first().value!!.generate() as String
            return x.substring(1,x.length-1)
        }
        else if(obj is JsonArray) {
            val x = obj.array.first().value!!.generate() as String
            return x.substring(1,x.length-1)
        }
        return "Undefined"
    }

    override fun excludeBooleans(obj: JsonValue): Boolean {
            if(obj.value is JsonBoolean)
                return true
        return false
    }

}

// ACTIONS

class EditProp : Action{

    override val name: String
        get() = "Edit"

    override fun execute(gui: JsonTree) {
        val shell = Shell(Display.getDefault())
        shell.setSize(250, 200)
        shell.text = "Edit Name"
        shell.layout = GridLayout(1,false)

        val label = Label(shell, SWT.NONE)
        val labelLayout = GridData()
        labelLayout.widthHint= 200
        labelLayout.heightHint = 300
        label.layoutData = labelLayout
        label.text = gui.tree.selection.first().data as String?

        val enterText = Text(shell, SWT.NONE)

        val button = Button(shell,SWT.PUSH)
        button.text = "Enter"

        button.addSelectionListener(object: SelectionAdapter() {
            override fun widgetSelected(e: SelectionEvent) {
                gui.tree.selection.first().text = enterText.text
                shell.dispose()
            }
        })


        shell.pack()
        shell.open()
        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        shell.dispose()


    }

}

class WriteToFile : Action{

    override val name: String
        get() = "File"

    override fun execute(gui: JsonTree) {
        val fileName = "jsonTeste"
        var file = File(fileName)
        file.writeText(gui.tree.selection.first().data as String)
    }

}

class ValidateNames : Action{

    override val name: String
        get() = "Validate"

    override fun execute(gui: JsonTree) {
        val dialog = MessageBox(gui.shell,SWT.ICON_INFORMATION)
        dialog.text = "INFO"

        gui.getAllTreeItems(gui.tree,gui.list)
        gui.list.forEach {
            if (it.text == null || it.text == ""){
                dialog.message = "INVALID"
                dialog.open()
                return
            }
        }

        dialog.message = "VALID"
        dialog.open()
    }

}

class Visualization : Action{

    override val name: String
        get() = "View"

    override fun execute(gui: JsonTree) {
        val shell = Shell(Display.getDefault())
        shell.setSize(250, 200)
        shell.text = "Edit"
        shell.layout = GridLayout(1,true)

        val label = Label(shell, SWT.NONE)
        val labelLayout = GridData()
        labelLayout.widthHint= 200
        labelLayout.heightHint = 400
        label.layoutData = labelLayout
        label.text = gui.tree.selection.first().data as String?


        shell.pack()
        shell.open()
        val display = Display.getDefault()
        while (!shell.isDisposed) {
            if (!display.readAndDispatch()) display.sleep()
        }
        shell.dispose()

    }

}




