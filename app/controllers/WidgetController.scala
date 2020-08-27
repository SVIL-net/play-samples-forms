package controllers

import javax.inject.Inject

import models._
import play.api.data._
import play.api.i18n._
import play.api.mvc._

import scala.collection._
import scala.concurrent.ExecutionContext
import org.checkerframework.checker.units.qual.A

/**
 * The classic WidgetController using MessagesAbstractController.
 *
 * Instead of MessagesAbstractController, you can use the I18nSupport trait,
 * which provides implicits that create a Messages instance from a request
 * using implicit conversion.
 *
 * See https://www.playframework.com/documentation/2.8.x/ScalaForms#passing-messagesprovider-to-form-helpers
 * for details.
 */
class WidgetController @Inject()(repo:WidgetRepository,
                                 cc: MessagesControllerComponents
)(implicit ec:ExecutionContext)
    extends MessagesAbstractController(cc) {
  import WidgetForm._

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.WidgetController.createWidget()
  private val computeUrl = routes.WidgetController.calc()

  def index = Action {
    Ok(views.html.index())
  }

  def listWidgets = Action.async { implicit request: MessagesRequest[AnyContent] =>
    repo.list().map { widgets =>
      // Pass an unpopulated form to the template
      Ok(views.html.listWidgets(widgets, form, postUrl))
    }
  }
  def show(total:Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    repo.list().map { widgets =>
      Ok(views.html.show(widgets.toSeq, total, countForm, computeUrl))
    }
  }
  def calc = Action.async { implicit request: MessagesRequest[AnyContent] =>
     val errorFunction = { formWithErrors: Form[Count] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
       repo.list().map { widgets =>
         BadRequest(views.html.show(widgets, 0, formWithErrors, computeUrl))
       }
    }
    val successFunction = { count: Count =>
      // This is the good case, where the form was successfully parsed as a Data object.
      repo.list().map { widgets =>
        val total = count.num.zip(widgets).foldRight(0)(
          (Z,b) => b+Z._1*Z._2.price
        )
        Redirect(routes.WidgetController.show(total))
      }
    }
    val formValidationResult = countForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  // This will be the action that handles our form post
  def createWidget = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      repo.list().map { widgets =>
        BadRequest(views.html.listWidgets(widgets, formWithErrors, postUrl))
      }
    }
    val successFunction = { data: Data =>
      // This is the good case, where the form was successfully parsed as a Data object.
      repo.create(data.name, data.price).map { _ =>
        Redirect(routes.WidgetController.listWidgets()).flashing("info" -> "Widget added!")
      }
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
