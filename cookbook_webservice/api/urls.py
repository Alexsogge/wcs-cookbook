from os.path import basename

from django.conf.urls import url, include
from django.urls import path
from django.contrib.auth.models import User
from rest_framework import routers, serializers, viewsets
from api import views
from rest_framework_jwt.views import obtain_jwt_token, refresh_jwt_token

router = routers.DefaultRouter()
router.register(r'tags', views.TagViewSet)
router.register(r'units', views.UnitViewSet)
router.register(r'ingredients', views.IngredientViewSet)
router.register(r'worksteps', views.WorkStepViewSet)
router.register(r'recipes', views.RecipeViewSet)
router.register(r'startsession', views.startNewSession)
router.register(r'getsessions', views.GetActiveSessions)



urlpatterns = [
    path('', include(router.urls)),
    path('recipe/<int:id>/', views.getRecipeSerialization),
    path('ingredient/<int:id>/', views.getIngredient),
    path('workstep/<int:step_id>/', views.getWorkstep),
    path(r'register/', views.RegisterUser.as_view()),
    #path('startsession/<int:recipe_id>', views.startNewSession),
    path('rest-auth/', include('rest_auth.urls')),
    path(r'api-token-auth/', obtain_jwt_token),
    path(r'api-token-refresh/', refresh_jwt_token),
]