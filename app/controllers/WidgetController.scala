package controllers

import javax.inject.Inject

import models._
import play.api.data._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}

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
class WidgetController @Inject()(repo: WidgetRepository,
                                  cc: MessagesControllerComponents
                                  )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import WidgetForm._

  private var total:Int = 0


  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.WidgetController.createWidget()
  private val calcUrl = routes.WidgetController.calcFunc()

  def index = Action {
    Ok(views.html.index())
  }

  def listWidgets = Action.async { implicit request =>
    repo.list().map{ widget =>
    // Pass an unpopulated form to the template
      Ok(views.html.listWidgets(widget, total, form, numForm, calcUrl))
    }
  }

  def calcFunc = Action.async { implicit request =>
    numForm.bindFromRequest.fold(
      errorForm => {
          Future.successful(Ok(views.html.listWidgets(Nil, total, form, errorForm, calcUrl)))
      },
      numbers => {
        repo.list().map{ widget =>
          val widgetsList = widget.toArray
          total = 0
          for {i <- 0 to widgetsList.length - 1}
            total += numbers.numbers(i) * widgetsList(i).price
          Redirect(routes.WidgetController.listWidgets())
        }
      }
    )
  }

  def getWidgets = Action.async { implicit request =>
    repo.list().map { widget =>
      Ok(Json.toJson(widget))
    }
  }

  def postWidgets = Action { implicit request =>
    // Pass an unpopulated form to the template
    Ok(views.html.postWidgets(form, postUrl))
  }

  // This will be the action that handles our form post
  def createWidget = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.postWidgets(errorForm, postUrl)))
      },
      widget => {
        repo.create(widget.name, widget.price).map { _ =>
          Redirect(routes.WidgetController.listWidgets).flashing("info" -> "Widget added!")
        }
      }
    )
  }
}
