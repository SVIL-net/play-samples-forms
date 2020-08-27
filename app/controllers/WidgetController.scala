package controllers

import javax.inject.Inject

import models._
import play.api.data._
import play.api.i18n._
import play.api.mvc._

import scala.collection._
import scala.concurrent.ExecutionContext

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

  // private val widgets = mutable.ArrayBuffer(
  //   Widget("Widget 1", 123),
  //   Widget("Widget 2", 456),
  //   Widget("Widget 3", 789)
  // )

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.WidgetController.createWidget()

  def index = Action {
    Ok(views.html.index())
  }

  def listWidgets = Action.async { implicit request: MessagesRequest[AnyContent] =>
    repo.list().map { widgets =>
      // Pass an unpopulated form to the template
      Ok(views.html.listWidgets(widgets, form, postUrl))
    }
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
