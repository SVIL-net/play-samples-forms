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



  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.WidgetController.createWidget()

  def index = Action {
    Ok(views.html.index())
  }


  def listWidgets = Action.async { implicit request: MessagesRequest[AnyContent] =>
    repo.list().map { widgets =>
    Ok(views.html.listWidgets(widgets, form, postUrl))
    }
  }


  def createWidget = Action{ implicit request =>
    Ok(views.html.createWidgets(form))
  }

  def addWidget = Action.async { implicit request =>

   form.bindFromRequest.fold(
    errorForm => {
      Future.successful(Ok(views.html.index()))
    },

    widgets => {
      repo.create(widgets.name, widgets.price).map { _ =>
        // Redirect(routes.WidgetController.createWidget()).flashing("success" -> "widget added")
     Redirect(routes.WidgetController.index()).flashing("success" -> "widget added")
      }
    }
    )
  }
  }
