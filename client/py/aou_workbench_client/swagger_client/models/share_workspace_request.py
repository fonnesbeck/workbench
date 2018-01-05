# coding: utf-8

"""
    AllOfUs Workbench API

    The API for the AllOfUs workbench.

    OpenAPI spec version: 0.1.0
    
    Generated by: https://github.com/swagger-api/swagger-codegen.git
"""


from pprint import pformat
from six import iteritems
import re


class ShareWorkspaceRequest(object):
    """
    NOTE: This class is auto generated by the swagger code generator program.
    Do not edit the class manually.
    """


    """
    Attributes:
      swagger_types (dict): The key is attribute name
                            and the value is attribute type.
      attribute_map (dict): The key is attribute name
                            and the value is json key in definition.
    """
    swagger_types = {
        'workspace_etag': 'str',
        'items': 'list[UserRole]'
    }

    attribute_map = {
        'workspace_etag': 'workspaceEtag',
        'items': 'items'
    }

    def __init__(self, workspace_etag=None, items=None):
        """
        ShareWorkspaceRequest - a model defined in Swagger
        """

        self._workspace_etag = None
        self._items = None
        self.discriminator = None

        if workspace_etag is not None:
          self.workspace_etag = workspace_etag
        self.items = items

    @property
    def workspace_etag(self):
        """
        Gets the workspace_etag of this ShareWorkspaceRequest.
        Associated workspace etag retrieved via reading the workspaces API. If provided, validates that the workspace (and its user roles) has not been modified since this etag was retrieved. 

        :return: The workspace_etag of this ShareWorkspaceRequest.
        :rtype: str
        """
        return self._workspace_etag

    @workspace_etag.setter
    def workspace_etag(self, workspace_etag):
        """
        Sets the workspace_etag of this ShareWorkspaceRequest.
        Associated workspace etag retrieved via reading the workspaces API. If provided, validates that the workspace (and its user roles) has not been modified since this etag was retrieved. 

        :param workspace_etag: The workspace_etag of this ShareWorkspaceRequest.
        :type: str
        """

        self._workspace_etag = workspace_etag

    @property
    def items(self):
        """
        Gets the items of this ShareWorkspaceRequest.

        :return: The items of this ShareWorkspaceRequest.
        :rtype: list[UserRole]
        """
        return self._items

    @items.setter
    def items(self, items):
        """
        Sets the items of this ShareWorkspaceRequest.

        :param items: The items of this ShareWorkspaceRequest.
        :type: list[UserRole]
        """
        if items is None:
            raise ValueError("Invalid value for `items`, must not be `None`")

        self._items = items

    def to_dict(self):
        """
        Returns the model properties as a dict
        """
        result = {}

        for attr, _ in iteritems(self.swagger_types):
            value = getattr(self, attr)
            if isinstance(value, list):
                result[attr] = list(map(
                    lambda x: x.to_dict() if hasattr(x, "to_dict") else x,
                    value
                ))
            elif hasattr(value, "to_dict"):
                result[attr] = value.to_dict()
            elif isinstance(value, dict):
                result[attr] = dict(map(
                    lambda item: (item[0], item[1].to_dict())
                    if hasattr(item[1], "to_dict") else item,
                    value.items()
                ))
            else:
                result[attr] = value

        return result

    def to_str(self):
        """
        Returns the string representation of the model
        """
        return pformat(self.to_dict())

    def __repr__(self):
        """
        For `print` and `pprint`
        """
        return self.to_str()

    def __eq__(self, other):
        """
        Returns true if both objects are equal
        """
        if not isinstance(other, ShareWorkspaceRequest):
            return False

        return self.__dict__ == other.__dict__

    def __ne__(self, other):
        """
        Returns true if both objects are not equal
        """
        return not self == other
